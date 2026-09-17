package com.fpt.backend.service.impl.signature;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.util.Store;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PadesVerificationService {

    public List<PadesVerificationResult> verifyAll(
            byte[] pdfBytes
    ) throws Exception {

        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalArgumentException("PDF is empty");
        }

        List<PadesVerificationResult> results =
                new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {

            List<PDSignature> signatures =
                    document.getSignatureDictionaries();

            if (signatures == null || signatures.isEmpty()) {
                return results;
            }

            System.out.println(
                    "========== PADES VERIFICATION =========="
            );

            System.out.println(
                    "Total signatures: " + signatures.size()
            );

            for (int i = 0; i < signatures.size(); i++) {

                PDSignature pdfSignature =
                        signatures.get(i);

                PadesVerificationResult result =
                        verifySingleSignature(
                                pdfSignature,
                                pdfBytes,
                                i + 1
                        );

                results.add(result);
            }

            System.out.println(
                    "========== END PADES VERIFICATION =========="
            );
        }

        return results;
    }

    private byte[] extractCmsSignature(
            PDSignature pdfSignature,
            byte[] pdfBytes
    ) throws Exception {

        byte[] contents =
                pdfSignature.getContents(pdfBytes);

        if (contents == null || contents.length == 0) {
            throw new IllegalArgumentException(
                    "PDF signature contents are empty"
            );
        }

        System.out.println(
                "Raw Contents size = " +
                        contents.length
        );

        /*
         * PDFBox getContents() đã trả về phần /Contents
         * của PDF.
         *
         * Với PDF hiện tại, CMS nằm ở đầu Contents,
         * phần còn lại là padding do signature placeholder.
         *
         * Không tự đọc ASN.1 length nữa.
         *
         * Thay vào đó dùng ASN.1InputStream của Bouncy Castle
         * để đọc chính xác object ASN.1 đầu tiên.
         */

        try (
                org.bouncycastle.asn1.ASN1InputStream asn1InputStream =
                        new org.bouncycastle.asn1.ASN1InputStream(
                                contents
                        )
        ) {

            org.bouncycastle.asn1.ASN1Primitive primitive =
                    asn1InputStream.readObject();

            if (primitive == null) {
                throw new IllegalArgumentException(
                        "CMS ASN.1 object is empty"
                );
            }

            byte[] cmsBytes =
                    primitive.getEncoded();

            System.out.println(
                    "Actual CMS size = " +
                            cmsBytes.length
            );

            System.out.println(
                    "CMS padding removed = " +
                            (contents.length - cmsBytes.length)
            );

            return cmsBytes;
        }
    }
    private PadesVerificationResult verifySingleSignature(
            PDSignature pdfSignature,
            byte[] pdfBytes,
            int signatureIndex
    ) throws Exception {

        System.out.println();
        System.out.println(
                "----- VERIFY SIGNATURE #" +
                        signatureIndex +
                        " -----"
        );


        byte[] cmsBytes =
                extractCmsSignature(
                        pdfSignature,
                        pdfBytes
                );

        if (cmsBytes == null || cmsBytes.length == 0) {
            return PadesVerificationResult.invalid(
                    signatureIndex,
                    "PDF signature contents are empty"
            );
        }

        System.out.println(
                "CMS size: " +
                        cmsBytes.length +
                        " bytes"
        );


        byte[] signedContent =
                pdfSignature.getSignedContent(pdfBytes);

        if (signedContent == null ||
                signedContent.length == 0) {

            return PadesVerificationResult.invalid(
                    signatureIndex,
                    "Signed PDF content is empty"
            );
        }

        System.out.println(
                "Signed content size: " +
                        signedContent.length +
                        " bytes"
        );

        CMSSignedData cms =
                new CMSSignedData(
                        new CMSProcessableByteArray(
                                signedContent
                        ),
                        cmsBytes
                );
        SignerInformationStore signerStore =
                cms.getSignerInfos();

        Collection<SignerInformation> signers =
                signerStore.getSigners();

        if (signers == null || signers.isEmpty()) {

            return PadesVerificationResult.invalid(
                    signatureIndex,
                    "CMS does not contain a signer"
            );
        }

        System.out.println(
                "Signer count: " +
                        signers.size()
        );

        SignerInformation signer =
                signers.iterator().next();


        Store<X509CertificateHolder> certificateStore =
                cms.getCertificates();

        Collection<X509CertificateHolder> matches =
                certificateStore.getMatches(
                        signer.getSID()
                );

        if (matches == null || matches.isEmpty()) {

            return PadesVerificationResult.invalid(
                    signatureIndex,
                    "Signer certificate was not found"
            );
        }

        X509CertificateHolder certificateHolder =
                matches.iterator().next();

        X509Certificate certificate =
                new JcaX509CertificateConverter()
                        .getCertificate(
                                certificateHolder
                        );


        String publicKeyAlgorithm =
                certificate
                        .getPublicKey()
                        .getAlgorithm();

        System.out.println(
                "Public Key Algorithm: " +
                        publicKeyAlgorithm
        );

        System.out.println(
                "Certificate Subject: " +
                        certificate
                                .getSubjectX500Principal()
                                .getName()
        );


        boolean valid =
                signer.verify(
                        new JcaSimpleSignerInfoVerifierBuilder()
                                .build(certificate)
                );

        System.out.println(
                "Signature valid: " +
                        valid
        );

        if (!valid) {

            return PadesVerificationResult.invalid(
                    signatureIndex,
                    "Digital signature verification failed"
            );
        }


        boolean certificateValid;

        try {
            certificate.checkValidity();
            certificateValid = true;
        } catch (Exception exception) {
            certificateValid = false;
        }

        System.out.println(
                "Certificate valid now: " +
                        certificateValid
        );


        return PadesVerificationResult.valid(
                signatureIndex,
                certificate.getSubjectX500Principal().getName(),
                publicKeyAlgorithm,
                certificate.getSerialNumber().toString(),
                certificate.getNotBefore(),
                certificate.getNotAfter(),
                certificateValid
        );
    }

    public record PadesVerificationResult(

            int signatureIndex,

            boolean valid,

            String signerSubject,

            String publicKeyAlgorithm,

            String certificateSerialNumber,

            java.util.Date certificateNotBefore,

            java.util.Date certificateNotAfter,

            boolean certificateValid,

            String message

    ) {

        public static PadesVerificationResult valid(
                int signatureIndex,
                String signerSubject,
                String publicKeyAlgorithm,
                String certificateSerialNumber,
                java.util.Date certificateNotBefore,
                java.util.Date certificateNotAfter,
                boolean certificateValid
        ) {
            return new PadesVerificationResult(
                    signatureIndex,
                    true,
                    signerSubject,
                    publicKeyAlgorithm,
                    certificateSerialNumber,
                    certificateNotBefore,
                    certificateNotAfter,
                    certificateValid,
                    "PAdES signature is valid"
            );
        }

        public static PadesVerificationResult invalid(
                int signatureIndex,
                String message
        ) {
            return new PadesVerificationResult(
                    signatureIndex,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    message
            );
        }
    }
}