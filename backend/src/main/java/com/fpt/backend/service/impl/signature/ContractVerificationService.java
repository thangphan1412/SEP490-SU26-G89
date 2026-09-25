package com.fpt.backend.service.impl.signature;

import com.fpt.backend.entity.Signature;
import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.signature.SignatureRepository;
import com.fpt.backend.service.impl.CloudinaryService;
import com.fpt.backend.service.interfaces.contract.ContractService;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractVerificationService {
    private final ContractService contractService;
    private final ContractRepository contractRepository;
    private final SignatureRepository signatureRepository;
    private final CloudinaryService cloudinaryService;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public VerificationReport verify(UUID contractId, String publicKeyCode) throws Exception {
        // Reuse the same VIEW authorization as Contract Details before reading files or keys.
        contractService.getContractById(contractId);
        if (publicKeyCode == null || !publicKeyCode.trim().matches("[0-9]{6}")) {
            throw new IllegalArgumentException("Enter the other signer's 6-digit public key code.");
        }
        var contract = contractRepository.findById(contractId).orElseThrow();
        if (contract.getDocumentFile() == null) {
            throw new IllegalArgumentException("The contract does not have a stored PDF");
        }
        var signatures = signatureRepository.findByContractIdOrderBySignatureCreateAtAsc(contractId);
        UUID viewerId = currentUser.getCurrentUser().getId();
        // Resolve only keys linked to actual signatures on this authorized contract.
        var matchingKeys = signatures.stream().map(Signature::getUserKey)
                .filter(key -> key != null && key.getUser() != null
                        && !viewerId.equals(key.getUser().getId())
                        && publicKeyCode.trim().equals(key.getKeyCode()))
                .map(UserKeys::getId).distinct().toList();
        if (matchingKeys.isEmpty()) {
            throw new IllegalArgumentException(
                    "This public key code does not match another signer on this contract.");
        }
        if (matchingKeys.size() != 1) {
            throw new IllegalArgumentException(
                    "This public key code matches multiple signing keys. The signer cannot be identified uniquely.");
        }
        byte[] pdf = cloudinaryService.download(contract.getDocumentFile());
        return verifyDocument(pdf, contract.getDocumentHash(), signatures,
                viewerId);
    }

    VerificationReport verifyDocument(byte[] pdf, String expectedHash,
                                      List<Signature> records, UUID viewerId) throws Exception {
        String actualHash = Base64.getEncoder().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(pdf));
        boolean storedHashMatches = expectedHash != null && MessageDigest.isEqual(
                actualHash.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                expectedHash.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        List<SignerResult> results = new ArrayList<>();
        var verifiedPdfSignatures = new java.util.HashSet<Integer>();
        boolean currentRevisionSigned = false;
        int pdfSignatureCount;
        try (var document = Loader.loadPDF(pdf)) {
            var pdfSignatures = document.getSignatureDictionaries();
            pdfSignatureCount = pdfSignatures.size();
            for (Signature record : records) {
                UserKeys key = record.getUserKey();
                boolean valid = false;
                boolean coversCurrentDocument = false;
                String fingerprint = null;
                String message = "The registered public key could not verify a signature in this PDF.";
                try {
                    byte[] encodedKey = Base64.getDecoder().decode(key.getPublicKey());
                    var publicKey = KeyFactory.getInstance("RSA").generatePublic(
                            new X509EncodedKeySpec(encodedKey));
                    fingerprint = HexFormat.ofDelimiter(":").withUpperCase().formatHex(
                            MessageDigest.getInstance("SHA-256").digest(encodedKey));
                    byte[] recordedValue = Base64.getDecoder().decode(record.getSignatureValue());
                    for (int signatureIndex = 0; signatureIndex < pdfSignatures.size(); signatureIndex++) {
                        if (verifiedPdfSignatures.contains(signatureIndex)) continue;
                        var pdfSignature = pdfSignatures.get(signatureIndex);
                        int[] range = pdfSignature.getByteRange();
                        if (range == null || range.length != 4 || range[0] != 0
                                || range[1] <= 0 || range[2] <= range[1] || range[3] < 0
                                || (long) range[2] + range[3] > pdf.length) {
                            continue;
                        }
                        try (var input = new ASN1InputStream(pdfSignature.getContents(pdf))) {
                            var cms = new CMSSignedData(new CMSProcessableByteArray(
                                    pdfSignature.getSignedContent(pdf)), input.readObject().getEncoded());
                            for (var signer : cms.getSignerInfos().getSigners()) {
                                // Use the key linked to the recorded signer, never a key supplied by the PDF.
                                if (MessageDigest.isEqual(recordedValue, signer.getSignature())
                                        && signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(publicKey))) {
                                    valid = true;
                                    verifiedPdfSignatures.add(signatureIndex);
                                    coversCurrentDocument |= (long) range[2] + range[3] == pdf.length;
                                }
                            }
                        } catch (Exception invalidSignature) {
                            // A wrong key, damaged CMS or changed signed bytes must never count as valid.
                        }
                        if (valid) break;
                    }
                    if (valid) {
                        message = coversCurrentDocument
                                ? "Signature valid: the signed bytes cover the current PDF revision."
                                : "Signature valid for an earlier revision. Later revisions exist; this signature alone does not verify them.";
                    }
                } catch (Exception invalidKey) {
                    message = "The signer's registered public key or signature record is missing or cannot be read.";
                }
                currentRevisionSigned |= valid && coversCurrentDocument;
                var user = key == null ? null : key.getUser();
                String signerName = user == null ? "Unknown signer"
                        : ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                        + (user.getLastName() == null ? "" : user.getLastName())).trim();
                if (user != null) {
                    signerName = signerName.isBlank() ? user.getEmail() : signerName + " (" + user.getEmail() + ")";
                }
                results.add(new SignerResult(record.getId(), user == null ? null : user.getId(),
                        signerName,
                        user != null && user.getId().equals(viewerId), valid,
                        coversCurrentDocument, fingerprint, message));
            }
        } catch (java.io.IOException invalidPdf) {
            return new VerificationReport(false, storedHashMatches, false, 0, List.of(),
                    "The stored file cannot be read as a PDF. Integrity could not be verified.");
        }
        boolean verified = storedHashMatches && currentRevisionSigned && !results.isEmpty()
                && pdfSignatureCount == records.size() && verifiedPdfSignatures.size() == pdfSignatureCount
                && results.stream().allMatch(SignerResult::valid);
        String message = verified
                ? "Registered signatures verified. The current PDF matches the stored document hash and has a valid signature covering its latest revision."
                : !storedHashMatches
                ? "The PDF differs from the stored document hash, or the original hash is unavailable. Integrity is not verified."
                : records.isEmpty() && pdfSignatureCount == 0
                ? "This contract has no digital signatures yet."
                : "Verification is incomplete or failed. A signature is missing or invalid, or later PDF changes are not verified.";
        return new VerificationReport(verified, storedHashMatches, currentRevisionSigned,
                pdfSignatureCount, results, message);
    }

    public record VerificationReport(boolean verified, boolean storedHashMatches,
                                     boolean currentRevisionSigned, int pdfSignatureCount,
                                     List<SignerResult> signatures, String message) {}

    public record SignerResult(UUID signatureId, UUID signerId, String signerName, boolean ownSignature,
                               boolean valid, boolean coversCurrentDocument,
                               String publicKeyFingerprint, String message) {}
}
