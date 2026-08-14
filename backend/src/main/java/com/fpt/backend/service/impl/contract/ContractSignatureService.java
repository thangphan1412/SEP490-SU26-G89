package com.fpt.backend.service.impl.contract;

import com.fpt.backend.entity.ContractWorkflowStepInstance;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.ElectronicSignatures;
import com.fpt.backend.entity.FileStorage;
import com.fpt.backend.entity.Signature;
import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.ElectronicStatus;
import com.fpt.backend.enums.SignatureAlgorithm;
import com.fpt.backend.enums.SignatureHash;
import com.fpt.backend.enums.SignatureStatus;
import com.fpt.backend.enums.SignatureType;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.electronicSignature.ElectronicSignatureRepository;
import com.fpt.backend.repository.signature.SignatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractSignatureService {
    private static final int MAX_PUBLIC_KEY_LENGTH = 10_000;
    private static final int MAX_DIGITAL_SIGNATURE_LENGTH = 10_000;
    private final SignatureRepository signatureRepository;
    private final ElectronicSignatureRepository electronicSignatureRepository;

    public Signature recordSignature(
            Contracts contract,
            ContractWorkflowStepInstance workflowStep,
            Users signer,
            UUID electronicSignatureId,
            String signerRole,
            byte[] approvedPdfContent,
            String digitalSignature
    ) {
        if (electronicSignatureId == null) {
            throw new BadHttpException(
                    "An active electronic signature must be selected"
            );
        }
        if (workflowStep != null
                && signatureRepository.existsByContractIdAndWorkflowStepInstanceId(
                contract.getId(),
                workflowStep.getId()
        )) {
            throw new BadHttpException(
                    "This workflow step has already been signed"
            );
        }

        ElectronicSignatures electronicSignature = electronicSignatureRepository
                .findByIdAndUserIdAndStatus(
                        electronicSignatureId,
                        signer.getId(),
                        ElectronicStatus.ACTIVE
                )
                .orElseThrow(() -> new BadHttpException(
                        "The selected electronic signature is unavailable or does not belong to you"
                ));
        FileStorage signatureFile = electronicSignature.getFileStorage();
        if (signatureFile == null
                || Boolean.TRUE.equals(signatureFile.getIsDeleted())
                || signatureFile.getFilePath() == null
                || signatureFile.getFilePath().isBlank()) {
            throw new BadHttpException(
                    "The selected electronic signature image is unavailable"
            );
        }
        UserKeys registeredUserKey = electronicSignature.getUserKey();
        if (registeredUserKey == null
                || registeredUserKey.getPublicKey() == null
                || registeredUserKey.getPublicKey().isBlank()) {
            throw new BadHttpException(
                    "The selected electronic signature has no registered RSA public key"
            );
        }
        if (registeredUserKey.getUser() == null
                || !signer.getId().equals(registeredUserKey.getUser().getId())) {
            throw new BadHttpException(
                    "The registered RSA key does not belong to the signer"
            );
        }

        String documentHash = calculatePdfHash(approvedPdfContent);
        VerifiedDigitalSignature verifiedDigitalSignature =
                verifyDigitalSignature(
                        approvedPdfContent,
                        registeredUserKey.getPublicKey(),
                        digitalSignature
                );
        if (registeredUserKey.getKeyFingerprint() != null
                && !registeredUserKey.getKeyFingerprint().equalsIgnoreCase(
                verifiedDigitalSignature.publicKeyFingerprint()
        )) {
            throw new BadHttpException(
                    "The registered RSA public key fingerprint is invalid"
            );
        }
        LocalDateTime now = LocalDateTime.now();
        return signatureRepository.save(Signature.builder()
                .signatureName(electronicSignature.getElectronicSignatureName())
                .signatureType(SignatureType.INTERNAL_RSA)
                .documentHash(documentHash)
                .signingPublicKey(verifiedDigitalSignature.publicKeyPem())
                .digitalSignature(verifiedDigitalSignature.signatureBase64())
                .publicKeyFingerprint(
                        verifiedDigitalSignature.publicKeyFingerprint()
                )
                .signatureAlgorithm(SignatureAlgorithm.RSA2048)
                .signatureHash(SignatureHash.SHA256)
                .status(SignatureStatus.SIGNED)
                .signatureUpdateAt(LocalDate.now())
                .signatureCreateAt(now)
                .signedAt(now)
                .signerRole(normalizeSignerRole(signerRole))
                .contract(contract)
                .signedBy(signer)
                .workflowStepInstance(workflowStep)
                .electronicSignatures(electronicSignature)
                .userKey(registeredUserKey)
                .fileStorage(signatureFile)
                .build());
    }

    public String calculatePdfHash(byte[] pdfContent) {
        if (pdfContent == null
                || pdfContent.length < 5
                || pdfContent[0] != '%'
                || pdfContent[1] != 'P'
                || pdfContent[2] != 'D'
                || pdfContent[3] != 'F'
                || pdfContent[4] != '-') {
            throw new BadHttpException(
                    "CEO approval must generate a valid PDF before signing"
            );
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(
                    pdfContent
            );
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private VerifiedDigitalSignature verifyDigitalSignature(
            byte[] pdfContent,
            String publicKeyPem,
            String digitalSignatureBase64
    ) {
        RegisteredPublicKey registeredPublicKey = validatePublicKey(
                publicKeyPem
        );
        String normalizedDigitalSignature = requireCredential(
                digitalSignatureBase64,
                MAX_DIGITAL_SIGNATURE_LENGTH,
                "A digital signature of the approved PDF is required"
        );

        try {
            byte[] signatureBytes = Base64.getDecoder().decode(
                    normalizedDigitalSignature.replaceAll("\\s", "")
            );

            java.security.Signature verifier = java.security.Signature
                    .getInstance("SHA256withRSA");
            verifier.initVerify(registeredPublicKey.publicKey());
            verifier.update(pdfContent);
            if (!verifier.verify(signatureBytes)) {
                throw new BadHttpException(
                        "The private key does not match the public key or approved PDF"
                );
            }

            return new VerifiedDigitalSignature(
                    registeredPublicKey.publicKeyPem(),
                    Base64.getEncoder().encodeToString(signatureBytes),
                    registeredPublicKey.publicKeyFingerprint()
            );
        } catch (BadHttpException exception) {
            throw exception;
        } catch (IllegalArgumentException | GeneralSecurityException exception) {
            throw new BadHttpException(
                    "The RSA public key or digital signature format is invalid"
            );
        }
    }

    public RegisteredPublicKey validatePublicKey(String publicKeyPem) {
        String normalizedPublicKey = requireCredential(
                publicKeyPem,
                MAX_PUBLIC_KEY_LENGTH,
                "An RSA public key is required"
        );
        try {
            byte[] publicKeyBytes = decodePublicKey(normalizedPublicKey);
            PublicKey publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            if (!(publicKey instanceof RSAPublicKey rsaPublicKey)) {
                throw new BadHttpException("An RSA public key is required");
            }
            int keySize = rsaPublicKey.getModulus().bitLength();
            if (keySize != 2048) {
                throw new BadHttpException(
                        "RSA public key must be exactly 2048 bits"
                );
            }
            return new RegisteredPublicKey(
                    toPublicKeyPem(publicKeyBytes),
                    calculateSha256(publicKeyBytes),
                    keySize,
                    rsaPublicKey
            );
        } catch (BadHttpException exception) {
            throw exception;
        } catch (IllegalArgumentException | GeneralSecurityException exception) {
            throw new BadHttpException("The RSA public key format is invalid");
        }
    }

    private byte[] decodePublicKey(String publicKeyPem) {
        String normalized = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        if (normalized.isBlank()
                || !publicKeyPem.contains("-----BEGIN PUBLIC KEY-----")
                || !publicKeyPem.contains("-----END PUBLIC KEY-----")) {
            throw new BadHttpException(
                    "Public key must use X.509 PEM format"
            );
        }
        return Base64.getDecoder().decode(normalized);
    }

    private String toPublicKeyPem(byte[] publicKeyBytes) {
        String encoded = Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(publicKeyBytes);
        return "-----BEGIN PUBLIC KEY-----\n"
                + encoded
                + "\n-----END PUBLIC KEY-----";
    }

    private String calculateSha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(content)
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String requireCredential(
            String value,
            int maximumLength,
            String missingMessage
    ) {
        if (value == null || value.isBlank()) {
            throw new BadHttpException(missingMessage);
        }
        String normalized = value.trim();
        if (normalized.length() > maximumLength) {
            throw new BadHttpException("Signing credential is too large");
        }
        return normalized;
    }

    private String normalizeSignerRole(String signerRole) {
        return signerRole == null || signerRole.isBlank()
                ? "SIGNER"
                : signerRole.trim().toUpperCase();
    }

    private record VerifiedDigitalSignature(
            String publicKeyPem,
            String signatureBase64,
            String publicKeyFingerprint
    ) {
    }

    public record RegisteredPublicKey(
            String publicKeyPem,
            String publicKeyFingerprint,
            int keySize,
            RSAPublicKey publicKey
    ) {
    }

}
