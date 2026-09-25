package com.fpt.backend.service.impl.signature;

import com.fpt.backend.entity.Signature;
import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.signature.SignatureRepository;
import com.fpt.backend.service.impl.CloudinaryService;
import com.fpt.backend.service.interfaces.contract.ContractService;
import com.fpt.backend.util.CurrentUser;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContractVerificationServiceTest {
    private final ContractService contracts = mock(ContractService.class);
    private final ContractRepository repository = mock(ContractRepository.class);
    private final SignatureRepository signatures = mock(SignatureRepository.class);
    private final CloudinaryService files = mock(CloudinaryService.class);
    private final CurrentUser currentUser = mock(CurrentUser.class);
    private final ContractVerificationService service = new ContractVerificationService(
            contracts, repository, signatures, files, currentUser);

    @Test
    void verifiesOtherSignerUsingRegisteredPublicKey() throws Exception {
        var signed = sign(blankPdf());
        var report = service.verifyDocument(signed.pdf(), hash(signed.pdf()),
                List.of(signed.record()), UUID.randomUUID());
        assertTrue(report.verified());
        assertFalse(report.signatures().getFirst().ownSignature());
        assertTrue(report.signatures().getFirst().coversCurrentDocument());
        assertNotNull(report.signatures().getFirst().publicKeyFingerprint());
    }

    @Test
    void rejectsChangedSignedBytesEvenWhenStoredHashMatchesChangedFile() throws Exception {
        var signed = sign(blankPdf());
        byte[] changed = signed.pdf().clone();
        // The PDF header version remains parseable, but a signed byte has changed.
        changed[7] = changed[7] == '7' ? (byte) '6' : (byte) '7';
        var report = service.verifyDocument(changed, hash(changed), List.of(signed.record()), UUID.randomUUID());
        assertFalse(report.verified());
        assertFalse(report.signatures().getFirst().valid());
    }

    @Test
    void rejectsWrongRegisteredPublicKey() throws Exception {
        var signed = sign(blankPdf());
        signed.record().getUserKey().setPublicKey(sign(blankPdf()).record().getUserKey().getPublicKey());
        var report = service.verifyDocument(signed.pdf(), hash(signed.pdf()), List.of(signed.record()), UUID.randomUUID());
        assertFalse(report.verified());
        assertFalse(report.signatures().getFirst().valid());
    }

    @Test
    void verifiesBothSignersAndDistinguishesEarlierRevision() throws Exception {
        var first = sign(blankPdf());
        var second = sign(first.pdf());
        var report = service.verifyDocument(second.pdf(), hash(second.pdf()),
                List.of(first.record(), second.record()), first.record().getUserKey().getUser().getId());
        assertTrue(report.verified());
        assertTrue(report.signatures().getFirst().ownSignature());
        assertFalse(report.signatures().getFirst().coversCurrentDocument());
        assertTrue(report.signatures().getLast().coversCurrentDocument());
    }

    @Test
    void rejectsUnsignedBytesAppendedAfterSignature() throws Exception {
        var signed = sign(blankPdf());
        var output = new ByteArrayOutputStream();
        output.write(signed.pdf());
        output.write("\n% appended after signing\n".getBytes(StandardCharsets.US_ASCII));
        byte[] changed = output.toByteArray();
        var report = service.verifyDocument(changed, hash(changed), List.of(signed.record()), UUID.randomUUID());
        assertTrue(report.signatures().getFirst().valid());
        assertFalse(report.currentRevisionSigned());
        assertFalse(report.verified());
    }

    @Test
    void rejectsStoredHashMismatchAndMissingSignerRecord() throws Exception {
        var signed = sign(blankPdf());
        assertFalse(service.verifyDocument(signed.pdf(), "wrong hash", List.of(signed.record()), UUID.randomUUID()).verified());
        assertFalse(service.verifyDocument(signed.pdf(), hash(signed.pdf()), List.of(), UUID.randomUUID()).verified());
    }

    @Test
    void unsignedAndUnreadableFilesAreNotVerified() throws Exception {
        byte[] pdf = blankPdf();
        assertFalse(service.verifyDocument(pdf, hash(pdf), List.of(), UUID.randomUUID()).verified());
        byte[] invalid = "not a PDF".getBytes(StandardCharsets.US_ASCII);
        assertFalse(service.verifyDocument(invalid, hash(invalid), List.of(), UUID.randomUUID()).verified());
    }

    @Test
    void checksContractViewPermissionBeforeReadingPdfOrKeys() {
        UUID id = UUID.randomUUID();
        when(contracts.getContractById(id)).thenThrow(new SecurityException("Forbidden"));
        assertThrows(SecurityException.class, () -> service.verify(id, "123456"));
        verifyNoInteractions(repository, signatures, files, currentUser);
    }

    private byte[] blankPdf() throws Exception {
        try (var document = new PDDocument(); var output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(output);
            return output.toByteArray();
        }
    }

    private SignedPdf sign(byte[] source) throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var pair = generator.generateKeyPair();
        var name = new X500Name("CN=Test signer");
        var certificate = new JcaX509v3CertificateBuilder(name, BigInteger.ONE,
                new Date(0), new Date(System.currentTimeMillis() + 86400000), name, pair.getPublic())
                .build(new JcaContentSignerBuilder("SHA256withRSA").build(pair.getPrivate()));
        var user = Users.builder().email("signer@example.test").build();
        user.setId(UUID.randomUUID());
        var key = UserKeys.builder().user(user)
                .publicKey(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded())).build();
        var record = Signature.builder().userKey(key).build();
        record.setId(UUID.randomUUID());
        try (var document = Loader.loadPDF(source); var output = new ByteArrayOutputStream()) {
            var signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ETSI_CADES_DETACHED);
            document.addSignature(signature, input -> {
                try {
                    var cmsGenerator = new CMSSignedDataGenerator();
                    cmsGenerator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                            new JcaDigestCalculatorProviderBuilder().build()).setDirectSignature(true)
                            .build(new JcaContentSignerBuilder("SHA256withRSA").build(pair.getPrivate()), certificate));
                    cmsGenerator.addCertificate(certificate);
                    var cms = cmsGenerator.generate(new CMSProcessableByteArray(input.readAllBytes()), false);
                    record.setSignatureValue(Base64.getEncoder().encodeToString(
                            cms.getSignerInfos().getSigners().iterator().next().getSignature()));
                    return cms.getEncoded();
                } catch (Exception exception) {
                    throw new java.io.IOException(exception);
                }
            });
            document.saveIncremental(output);
            return new SignedPdf(output.toByteArray(), record);
        }
    }

    private String hash(byte[] pdf) throws Exception {
        return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(pdf));
    }

    private record SignedPdf(byte[] pdf, Signature record) {}
}
