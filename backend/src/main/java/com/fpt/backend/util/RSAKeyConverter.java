package com.fpt.backend.util;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;
@Component
public class RSAKeyConverter {
//    public static String encode(BigInteger modulus, BigInteger exponent
//    ) {
//        String key = modulus.toString(16)
//                        + ":"
//                        + exponent.toString(16);
//        return Base64.getEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8));
//    }
//    public static BigInteger[] decode(String encodedKey) {
//        String key = new String(Base64.getDecoder()
//                                .decode(encodedKey), StandardCharsets.UTF_8);
//        String[] parts = key.split(":");
//        if (parts.length != 2) {
//            throw new IllegalArgumentException(
//                    "Invalid RSA key format"
//            );
//        }
//        BigInteger modulus = new BigInteger(parts[0], 16);
//        BigInteger exponent = new BigInteger(parts[1], 16);
//        return new BigInteger[]{modulus, exponent};
//    }
private RSAKeyConverter() {
}

    public static String encodePKCS8(
            BigInteger n,
            BigInteger e,
            BigInteger d,
            BigInteger p,
            BigInteger q,
            BigInteger dP,
            BigInteger dQ,
            BigInteger qInv
    ) throws IOException {

        RSAPrivateKey rsaPrivateKey = new RSAPrivateKey(
                n,
                e,
                d,
                p,
                q,
                dP,
                dQ,
                qInv
        );


        AlgorithmIdentifier algorithmIdentifier =
                new AlgorithmIdentifier(
                        PKCSObjectIdentifiers.rsaEncryption,
                        DERNull.INSTANCE
                );

        PrivateKeyInfo privateKeyInfo =
                new PrivateKeyInfo(
                        algorithmIdentifier,
                        rsaPrivateKey
                );

        byte[] derBytes = privateKeyInfo.getEncoded();

        return Base64.getEncoder().encodeToString(derBytes);
    }

}
