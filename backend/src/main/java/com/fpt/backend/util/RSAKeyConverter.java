package com.fpt.backend.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RSAKeyConverter {
    private RSAKeyConverter() {
    }
    public static String encode(BigInteger modulus, BigInteger exponent
    ) {
        String key = modulus.toString(16)
                        + ":"
                        + exponent.toString(16);
        return Base64.getEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8));
    }
    public static BigInteger[] decode(String encodedKey) {
        String key = new String(Base64.getDecoder()
                                .decode(encodedKey), StandardCharsets.UTF_8);
        String[] parts = key.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid RSA key format"
            );
        }
        BigInteger modulus = new BigInteger(parts[0], 16);
        BigInteger exponent = new BigInteger(parts[1], 16);
        return new BigInteger[]{modulus, exponent};
    }
}
