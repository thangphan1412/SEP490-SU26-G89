package com.fpt.backend.service.impl.signature;

import com.fpt.backend.dto.request.signature.PadesSigningSession;
import com.fpt.backend.dto.response.signature.PadesPrepareResponse;
import com.fpt.backend.service.impl.contract.ContractWorkflowRules;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.FileStorage;
import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractStatusHistoryRepository;
import com.fpt.backend.repository.contract.ContractWorkflowStepInstanceRepository;
import com.fpt.backend.repository.signature.UserKeysRepository;
import com.fpt.backend.service.impl.CloudinaryService;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.util.Hex;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PadesSigningService {


    private final UserKeysRepository userKeysRepository;
    private final CloudinaryService cloudinaryService;
    private final ContractRepository contractRepository;
    private final CurrentUser currentUser;


    private final Map<UUID, PadesSigningSession> sessions = new ConcurrentHashMap<>();

    public PadesPrepareResponse prepare(
            UUID contractId,
            UUID userId,
            UUID electronicSignatureId,
            String keyCode,
            byte[] pdfBytes
    ) throws Exception {
        validatePrepareRequest(
                contractId,
                userId,
                electronicSignatureId,
                keyCode,
                pdfBytes
        );
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName("Electronic Signature");
            signature.setReason("Contract signing");
            signature.setSignDate(Calendar.getInstance());
            document.addSignature(signature);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ExternalSigningSupport externalSigning = document.saveIncrementalForExternalSigning(output);
            byte[] contentToSign = externalSigning
                            .getContent()
                            .readAllBytes();
            byte[] hash = MessageDigest.getInstance("SHA-256")
                            .digest(contentToSign);
            String documentHash = Base64.getEncoder()
                            .encodeToString(hash);

            externalSigning.setSignature(
                    new byte[0]
            );

            int signatureOffset = signature.getByteRange()[1] + 1;
            int signatureCapacity = signature.getByteRange()[2] - signature.getByteRange()[1] - 2;
            byte[] preparedPdf = output.toByteArray();
            UUID sessionId = UUID.randomUUID();
            PadesSigningSession session = PadesSigningSession.builder()
                            .sessionId(sessionId)
                            .contractId(contractId)
                            .userId(userId)
                            .electronicSignatureId(electronicSignatureId)
                            .keyCode(keyCode)
                            .preparedPdf(preparedPdf)
                            .contentToSign(contentToSign)
                            .documentHash(documentHash)
                            .signatureOffset(signatureOffset)
                            .signatureCapacity(signatureCapacity)
                            .build();
            sessions.put(sessionId, session);
            return new PadesPrepareResponse(
                    sessionId,
                    documentHash,
                    Base64.getEncoder()
                            .encodeToString(contentToSign),
                    "SHA-256",
                    "RSA",
                    keyCode
            );
        }
    }

    public byte[] complete(
            UUID sessionId,
            String signatureValue
    ) throws Exception {
        System.out.println("========== COMPLETE CALLED ==========");
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID is required");
        }
        if (signatureValue == null ||
                signatureValue.isBlank()) {
            throw new IllegalArgumentException("Signature value is required");
        }

        System.out.println("========== PADES COMPLETE ==========");
        System.out.println("Session ID: " + sessionId);
        System.out.println("Signature Value exists: " + (signatureValue != null && !signatureValue.isBlank()));
        PadesSigningSession session = sessions.get(sessionId);
        System.out.println("Session exists: " + (session != null));
        if (session != null) {
            System.out.println("Session contractId: " + session.getContractId());
            System.out.println("Session userId: " + session.getUserId());
            System.out.println("Session keyCode: " + session.getKeyCode());
        }
        if (session == null) {
            throw new IllegalArgumentException("Signing session not found or expired");
        }

        UserKeys userKeys = userKeysRepository.findByUserIdAndKeyCode(
                                session.getUserId(),
                                session.getKeyCode()
                        ).orElseThrow(() -> new IllegalArgumentException("User key not found for keyCode: " + session.getKeyCode()));

        System.out.println("========== USER KEY ==========");
        System.out.println("DB keyCode: " + userKeys.getKeyCode());
        System.out.println("Certificate: " + (userKeys.getCertificate() != null && !userKeys.getCertificate().isBlank()));
        System.out.println("==============================");
        if (!session.getKeyCode().equals(userKeys.getKeyCode())) {
            throw new IllegalArgumentException("Key code does not match signing session");
        }

        X509Certificate certificate = loadCertificate(userKeys.getCertificate());
        byte[] rsaSignature = Base64.getDecoder().decode(signatureValue);

        System.out.println("========== PADES VERIFY ==========");
        System.out.println("Session ID: " + sessionId);
        System.out.println("Key Code: " + session.getKeyCode());
        System.out.println("Content To Sign Length: " + session.getContentToSign().length);
        System.out.println("Signature Length: " + rsaSignature.length);

        boolean valid = verifyBrowserSignature(
                        session.getContentToSign(),
                        rsaSignature,
                        certificate
                );
        if (!valid) {
            throw new IllegalArgumentException("Invalid digital signature");
        }
        System.out.println("Signature valid: " + valid);
        System.out.println("=================================");
//        byte[] cmsSignature = createCmsSignature(
//                        session.getContentToSign(),
//                        rsaSignature,
//                        certificate
//                );
        System.out.println("========== BEFORE CREATE CMS ==========");
        byte[] cmsSignature =
                createCmsSignature(
                        session.getContentToSign(),
                        rsaSignature,
                        certificate
                );
        System.out.println("========== AFTER CREATE CMS ==========");
        System.out.println("CMS size = " + cmsSignature.length);

        debugCms(cmsSignature);

        byte[] cmsHex = Hex.getBytes(cmsSignature);
        if (cmsHex.length > session.getSignatureCapacity()) {
            throw new IllegalStateException("CMS signature is larger than PDF signature placeholder");
        }
        byte[] signedPdf = session.getPreparedPdf().clone();

        System.arraycopy(
                cmsHex,
                0,
                signedPdf,
                session.getSignatureOffset(),
                cmsHex.length
        );

        Contracts contract = contractRepository
                        .findById(session.getContractId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("Contract not found: " + session.getContractId())
                        );
        Users user = currentUser.getCurrentUser();
        if (user == null || !user.getId().equals(session.getUserId())) {
            throw new IllegalArgumentException("Current user does not match signing session");
        }

        String originalName = contract.getDocumentFile() != null
                ? contract.getDocumentFile().getOriginalName()
                : "contract-" + contract.getContractNumber() + ".pdf";
        if (!originalName.toLowerCase().endsWith(".pdf")) {
            originalName = originalName + ".pdf";
        }
        FileStorage newFileStorage = cloudinaryService.uploadPdfAndSave(
                        signedPdf,
                        originalName,
                        user
                );
        contract.setDocumentFile(newFileStorage);
        contract.setDocumentHash(
                Base64.getEncoder().encodeToString(
                        MessageDigest.getInstance("SHA-256")
                                .digest(signedPdf)
                )
        );
        contractRepository.save(contract);
        sessions.remove(sessionId);
        System.out.println("========== PADES COMPLETE SUCCESS ==========");
        System.out.println("Contract ID      = " + contract.getId());
        System.out.println("New FileStorage  = " + newFileStorage.getId());
        System.out.println("Signed PDF bytes = " + signedPdf.length);
        return signedPdf;
    }

    private boolean verifyBrowserSignature(
            byte[] content,
            byte[] signature,
            X509Certificate certificate
    ) throws Exception {
        java.security.Signature verifier =
                java.security.Signature.getInstance(
                        "SHA256withRSA"
                );
        verifier.initVerify(certificate.getPublicKey());
        verifier.update(content);
        return verifier.verify(signature);
    }
    private void debugCms(byte[] cmsBytes) throws Exception {

        System.out.println("\n========== DEBUG CMS ==========");

        System.out.println("CMS size: " + cmsBytes.length + " bytes");

        CMSSignedData cms = new CMSSignedData(cmsBytes);

        // 1. Kiểm tra content
        System.out.println(
                "Encapsulated content: "
                        + (cms.getSignedContent() != null)
        );

        // 2. Certificate
        Store<X509CertificateHolder> certificateStore =
                cms.getCertificates();

        System.out.println(
                "Certificate count: "
                        + certificateStore.getMatches(null).size()
        );

        // 3. SignerInfo
        Collection<SignerInformation> signers =
                cms.getSignerInfos().getSigners();

        System.out.println(
                "Signer count: "
                        + signers.size()
        );

        for (SignerInformation signer : signers) {

            System.out.println("\n----- SignerInfo -----");

            System.out.println(
                    "Digest Algorithm: "
                            + signer.getDigestAlgOID()
            );

            System.out.println(
                    "Encryption Algorithm: "
                            + signer.getEncryptionAlgOID()
            );

            System.out.println(
                    "Signature length: "
                            + signer.getSignature().length
                            + " bytes"
            );

            System.out.println(
                    "Signed Attributes: "
                            + (signer.getSignedAttributes() != null)
            );

            System.out.println(
                    "Unsigned Attributes: "
                            + (signer.getUnsignedAttributes() != null)
            );

            System.out.println(
                    "Content Type Attribute: "
                            + (signer.getSignedAttributes() != null
                            && signer.getSignedAttributes()
                            .get(CMSAttributes.contentType) != null)
            );

            System.out.println(
                    "Message Digest Attribute: "
                            + (signer.getSignedAttributes() != null
                            && signer.getSignedAttributes()
                            .get(CMSAttributes.messageDigest) != null)
            );
        }

        System.out.println("========== END DEBUG CMS ==========\n");
    }
    private byte[] createCmsSignature(
            byte[] content,
            byte[] rsaSignature,
            X509Certificate certificate
    ) throws Exception {
        ContentSigner externalContentSigner = new ContentSigner() {
                    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    @Override
                    public org.bouncycastle.asn1.x509.AlgorithmIdentifier
                    getAlgorithmIdentifier() {
                        return new org.bouncycastle.asn1.x509.AlgorithmIdentifier(
                                org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.sha256WithRSAEncryption
                        );
                    }
                    @Override
                    public java.io.OutputStream
                    getOutputStream() {
                        return outputStream;
                    }
                    @Override
                    public byte[] getSignature() {
                        return rsaSignature;
                    }
                };
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        var digestProvider = new JcaDigestCalculatorProviderBuilder().build();
        SignerInfoGenerator signerInfoGenerator = new JcaSignerInfoGeneratorBuilder(digestProvider)
                        .setDirectSignature(true)
                        .build(
                                externalContentSigner,
                                certificate
                        );
        generator.addSignerInfoGenerator(
                signerInfoGenerator
        );
        generator.addCertificates(
                new JcaCertStore(java.util.List.of(certificate))
        );

        CMSSignedData signedData = generator.generate(new CMSProcessableByteArray(content), false);
        return signedData.getEncoded();
    }

    private X509Certificate loadCertificate(
            String certificateBase64
    ) throws Exception {

        if (certificateBase64 == null || certificateBase64.isBlank()) {
            throw new IllegalArgumentException("Certificate not found");
        }

        byte[] certificateBytes =
                Base64.getDecoder()
                        .decode(certificateBase64);

        CertificateFactory factory =
                CertificateFactory.getInstance(
                        "X.509"
                );

        return (X509Certificate)
                factory.generateCertificate(
                        new ByteArrayInputStream(
                                certificateBytes
                        )
                );
    }

    private void validatePrepareRequest(
            UUID contractId,
            UUID userId,
            UUID electronicSignatureId,
            String keyCode,
            byte[] pdfBytes
    ) {

        if (contractId == null) {
            throw new IllegalArgumentException(
                    "Contract ID is required"
            );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        if (electronicSignatureId == null) {
            throw new IllegalArgumentException(
                    "Electronic signature ID is required"
            );
        }

        if (keyCode == null ||
                keyCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Key code is required"
            );
        }

        if (pdfBytes == null ||
                pdfBytes.length == 0) {

            throw new IllegalArgumentException(
                    "PDF is empty"
            );
        }
    }


    public PadesSigningSession getSession(
            UUID sessionId
    ) {
        PadesSigningSession session =
                sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException(
                    "Signing session not found or expired"
            );
        }

        return session;
    }

}