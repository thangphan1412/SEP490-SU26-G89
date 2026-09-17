package com.fpt.backend.util;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

public final class CertificateGenerator {

    private CertificateGenerator() {
    }

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static X509Certificate generate(
            PublicKey publicKey,
            PrivateKey privateKey,
            String keyCode
    ) throws Exception {

        long now = System.currentTimeMillis();

        Date notBefore = new Date(now - 60_000);

        Date notAfter = new Date(
                now + 365L * 24 * 60 * 60 * 1000
        );

        BigInteger serialNumber =
                new BigInteger(
                        128,
                        new java.security.SecureRandom()
                );

        X500Name subject =
                new X500Name(
                        "CN=Electronic Signature " + keyCode
                );

        JcaX509v3CertificateBuilder builder =
                new JcaX509v3CertificateBuilder(
                        subject,
                        serialNumber,
                        notBefore,
                        notAfter,
                        subject,
                        publicKey
                );

        ContentSigner contentSigner =
                new JcaContentSignerBuilder("SHA256withRSA")
                        .setProvider("BC")
                        .build(privateKey);

        X509CertificateHolder holder =
                builder.build(contentSigner);

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(holder);
    }
}