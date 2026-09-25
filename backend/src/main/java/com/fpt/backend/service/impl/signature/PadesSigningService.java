package com.fpt.backend.service.impl.signature;

import com.fpt.backend.dto.request.signature.PadesSigningSession;
import com.fpt.backend.dto.response.signature.PadesPrepareResponse;
import com.fpt.backend.entity.*;
import com.fpt.backend.repository.electronicSignature.ElectronicSignatureRepository;
import com.fpt.backend.service.impl.contract.ContractWorkflowRules;
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
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
@Service
@RequiredArgsConstructor
public class PadesSigningService {
    private final UserKeysRepository userKeysRepository;
    private final CloudinaryService cloudinaryService;
    private final ContractRepository contractRepository;
    private final CurrentUser currentUser;
    private final PadesVerificationService padesVerificationService;
    private final ElectronicSignatureRepository electronicSignatureRepository;
    private final com.fpt.backend.service.interfaces.contract.ContractService contractService;
    private final Map<UUID, PadesSigningSession> sessions = new ConcurrentHashMap<>();

    public PadesPrepareResponse prepare(
            UUID contractId,
            UUID userId,
            UUID electronicSignatureId,
            String keyCode,
            int pageNumber,
            float positionX,
            float positionY,
            float signatureWidth,
            float signatureHeight,

            byte[] pdfBytes
    ) throws Exception {
        validatePrepareRequest(
                contractId,
                userId,
                electronicSignatureId,
                keyCode,
                pageNumber,
                positionX,
                positionY,
                signatureWidth,
                signatureHeight,
                pdfBytes
        );
        Users signer = currentUser.getCurrentUser();
        var detail = contractService.getContractById(contractId);
        var workflow = detail.workflowRuntime();
        if (workflow == null || !signer.getId().equals(userId)
                || !userId.equals(workflow.currentAssignedUserId())
                || !java.util.List.of("SIGN", "APPROVE_AND_SIGN").contains(workflow.currentStepActionType())
                || !workflow.availableActions().contains("COMPLETE_STEP")) {
            throw new IllegalArgumentException("You are not assigned to the current signing step");
        }
        if (signer.getDob() == null || java.time.Period.between(
                java.time.LocalDate.parse(signer.getDob()), java.time.LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("A valid date of birth and minimum age of 18 are required to sign");
        }
        var sourceContract = contractRepository.findById(contractId).orElseThrow();
        String sourceHash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(pdfBytes));
        if (!sourceHash.equals(sourceContract.getDocumentHash())) {
            throw new IllegalArgumentException("The PDF has changed. Reload the contract before signing");
        }
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {

            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName(signerDisplayName(signer));
            signature.setReason("Contract signing");
            signature.setSignDate(Calendar.getInstance());
            drawSignatureImage(
                    document,
                    electronicSignatureId,
                    pageNumber,
                    positionX,
                    positionY,
                    signatureWidth,
                    signatureHeight);
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
                    .originalPdf(pdfBytes)
                    .preparedPdf(preparedPdf)
                    .contentToSign(contentToSign)
                    .documentHash(documentHash)
                    .signatureOffset(signatureOffset)
                    .signatureCapacity(signatureCapacity)
                    .pageNumber(pageNumber)
                    .positionX(positionX)
                    .positionY(positionY)
                    .signatureWidth(signatureWidth)
                    .signatureHeight(signatureHeight)
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

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
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
        System.out.println(
                "========== VERIFY EXISTING PADES SIGNATURES =========="
        );

        List<PadesVerificationService.PadesVerificationResult>
                existingSignatures =
                padesVerificationService.verifyAll(
                        session.getOriginalPdf()
                );

        System.out.println(
                "Existing signatures: " +
                        existingSignatures.size()
        );

        for (PadesVerificationService.PadesVerificationResult result
                : existingSignatures) {

            System.out.println(
                    "Signature #" +
                            result.signatureIndex() +
                            " valid = " +
                            result.valid()
            );

            if (!result.valid()) {
                throw new IllegalArgumentException(
                        "Existing PAdES signature #" +
                                result.signatureIndex() +
                                " is invalid. " +
                                result.message()
                );
            }
        }

        System.out.println(
                "All existing PAdES signatures are valid"
        );

        System.out.println(
                "====================================================="
        );
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
                        .findForSigningById(session.getContractId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("Contract not found: " + session.getContractId())
                        );
        Users user = currentUser.getCurrentUser();
        if (user == null || !user.getId().equals(session.getUserId())) {
            throw new IllegalArgumentException("Current user does not match signing session");
        }
        String originalHash = Base64.getEncoder().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(session.getOriginalPdf()));
        if (!originalHash.equals(contract.getDocumentHash())) {
            throw new IllegalArgumentException("The contract changed during signing. Reload it and try again");
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
        // Persist the signature record, workflow step and history in this same transaction.
        var updated = contractService.transitionContract(contract.getId(),
                new com.fpt.backend.dto.request.contract.ContractTransitionRequest(
                        "COMPLETE_STEP", null, null, null, signatureValue,
                        session.getElectronicSignatureId(), session.getKeyCode(), null));
        if ("CANCELLED".equals(updated.contractStatus())) {
            throw new IllegalArgumentException("Signing requirements were not met; the signature was not saved");
        }
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
        System.out.println("Encapsulated content: " + (cms.getSignedContent() != null));
        Store<X509CertificateHolder> certificateStore = cms.getCertificates();
        System.out.println("Certificate count: " + certificateStore.getMatches(null).size());

        Collection<SignerInformation> signers = cms.getSignerInfos().getSigners();
        System.out.println("Signer count: " + signers.size());

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
            int pageNumber,
            float positionX,
            float positionY,
            float signatureWidth,
            float signatureHeight,
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
        if (pageNumber <= 0) {
            throw new IllegalArgumentException(
                    "Page number must be greater than 0"
            );
        }

        if (positionX < 0) {
            throw new IllegalArgumentException(
                    "Position X cannot be negative"
            );
        }

        if (positionY < 0) {
            throw new IllegalArgumentException(
                    "Position Y cannot be negative"
            );
        }

        if (signatureWidth <= 0) {
            throw new IllegalArgumentException(
                    "Signature width must be greater than 0"
            );
        }

        if (signatureHeight <= 0) {
            throw new IllegalArgumentException(
                    "Signature height must be greater than 0"
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
    private void drawSignatureImage(
            PDDocument document,
            UUID electronicSignatureId,
            int pageNumber,
            float positionX,
            float positionY,
            float signatureWidth,
            float signatureHeight
    ) throws Exception {

        // ==========================================
        // 1. VALIDATE PAGE
        // ==========================================

        if (pageNumber <= 0 || pageNumber > document.getNumberOfPages()) {
            throw new IllegalArgumentException(
                    "Invalid page number: " + pageNumber
            );
        }

        // ==========================================
        // 2. GET ELECTRONIC SIGNATURE
        // ==========================================

        ElectronicSignatures electronicSignature =
                electronicSignatureRepository.findOwnedById(electronicSignatureId, currentUser.getCurrentUser().getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Electronic signature not found: "
                                                + electronicSignatureId
                                )
                        );

        if (electronicSignature.getStatus() != com.fpt.backend.enums.ElectronicStatus.ACTIVE) {
            throw new IllegalArgumentException("Only an active electronic signature can be used");
        }
        FileStorage fileStorage =
                electronicSignature.getFileStorage();

        if (fileStorage == null) {
            throw new IllegalArgumentException(
                    "Electronic signature has no file"
            );
        }

        String fileUrl = fileStorage.getFilePath();

        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Electronic signature image URL is empty"
            );
        }

        System.out.println("========== SIGNATURE IMAGE ==========");
        System.out.println("Signature ID = " + electronicSignatureId);
        System.out.println("File URL     = " + fileUrl);
        System.out.println("Page         = " + pageNumber);
        System.out.println("X            = " + positionX);
        System.out.println("Y            = " + positionY);
        System.out.println("Width        = " + signatureWidth);
        System.out.println("Height       = " + signatureHeight);
        System.out.println("=====================================");

        // ==========================================
        // 3. DOWNLOAD IMAGE
        // ==========================================

        byte[] imageBytes;

        try (java.io.InputStream inputStream =
                     new java.net.URL(fileUrl).openStream()) {

            imageBytes = inputStream.readAllBytes();
        }

        System.out.println(
                "Signature image bytes = " + imageBytes.length
        );

        if (imageBytes.length == 0) {
            throw new IllegalArgumentException(
                    "Signature image is empty"
            );
        }

        // ==========================================
        // 4. GET PDF PAGE
        // ==========================================

        PDPage page = document.getPage(pageNumber - 1);

        float pdfPageWidth =
                page.getMediaBox().getWidth();

        float pdfPageHeight =
                page.getMediaBox().getHeight();

        System.out.println(
                "PDF page size = "
                        + pdfPageWidth
                        + " x "
                        + pdfPageHeight
        );

        // ==========================================
        // 5. FRONTEND PAGE SIZE
        // ==========================================

        float renderedPageWidth = 750f;


        float scale = pdfPageWidth / renderedPageWidth;

        // ==========================================
        // 6. CONVERT POSITION
        // ==========================================

        float pdfX = positionX * scale;
        float pdfWidth = signatureWidth * scale;
        float pdfHeight = signatureHeight * scale;


        float pdfY = pdfPageHeight - (positionY * scale) - pdfHeight;

        // ==========================================
        // 7. KEEP INSIDE PAGE
        // ==========================================

        pdfX = Math.max(
                0,
                Math.min(
                        pdfX,
                        pdfPageWidth - pdfWidth
                )
        );

        pdfY = Math.max(
                0,
                Math.min(
                        pdfY,
                        pdfPageHeight - pdfHeight
                )
        );

        System.out.println("========== PDF POSITION ==========");
        System.out.println("Scale           = " + scale);
        System.out.println("PDF X           = " + pdfX);
        System.out.println("PDF Y           = " + pdfY);
        System.out.println("PDF width       = " + pdfWidth);
        System.out.println("PDF height      = " + pdfHeight);
        System.out.println("==================================");

        // ==========================================
        // 8. CREATE PDF IMAGE
        // ==========================================

        PDImageXObject image =
                PDImageXObject.createFromByteArray(
                        document,
                        imageBytes,
                        "electronic-signature"
                );

        // ==========================================
        // 9. DRAW IMAGE
        // ==========================================

        try (PDPageContentStream contentStream =
                     new PDPageContentStream(
                             document,
                             page,
                             PDPageContentStream.AppendMode.APPEND,
                             true,
                             true
                     )) {

            float labelHeight = Math.min(30f, pdfHeight * 0.55f);
            float imageScale = Math.min(pdfWidth / image.getWidth(), (pdfHeight - labelHeight) / image.getHeight());
            float imageWidth = image.getWidth() * imageScale;
            float imageHeight = image.getHeight() * imageScale;
            contentStream.drawImage(
                    image,
                    pdfX + (pdfWidth - imageWidth) / 2,
                    pdfY + labelHeight,
                    imageWidth,
                    imageHeight
            );
            var font = com.fpt.backend.service.impl.contract.ContractPdfGenerator.loadUnicodeFont(document, false);
            String[] lines = {
                    "Đã ký: " + signerDisplayName(electronicSignature.getUser()),
                    electronicSignature.getUser().getEmail(),
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            };
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i] == null ? "" : lines[i].replaceAll("[\\r\\n\\t]", " ");
                float textWidth = font.getStringWidth(line) / 1000f;
                float lineHeight = labelHeight / lines.length;
                float fontSize = Math.min(lineHeight * 0.8f, pdfWidth / Math.max(1f, textWidth));
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset(pdfX, pdfY + labelHeight - lineHeight * (i + 0.8f));
                contentStream.showText(line);
                contentStream.endText();
            }
        }
        page.getCOSObject().setNeedToBeUpdated(true);
        document.getPages().getCOSObject().setNeedToBeUpdated(true);

        System.out.println(
                "========== SIGNATURE IMAGE DRAWN =========="
        );
    }

    private String signerDisplayName(Users user) {
        String name = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return name.isBlank() ? user.getEmail() : name;
    }
}
