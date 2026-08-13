package com.fpt.backend.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RSAKeyConverter {
    private RSAKeyConverter() {
    }

    public static String encode(
            BigInteger modulus,
            BigInteger exponent
    ) {

        String value =
                modulus.toString(16)
                        + ":"
                        + exponent.toString(16);

        return Base64.getEncoder()
                .encodeToString(
                        value.getBytes(StandardCharsets.UTF_8)
                );
    }

    public static BigInteger[] decode(String key) {

        String value = new String(
                Base64.getDecoder().decode(key),
                StandardCharsets.UTF_8
        );

        String[] parts = value.split(":");

        return new BigInteger[]{
                new BigInteger(parts[0], 16),
                new BigInteger(parts[1], 16)
        };
    }
}
