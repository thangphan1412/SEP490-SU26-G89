package com.fpt.backend.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.SecureRandom;

@Component
public class CalculateRSA {
    private static final int KEY_SIZE = 2048;

    public RSAKeyPair generateKeyPair() {

        SecureRandom random = new SecureRandom();

        BigInteger p;
        BigInteger q;

        do {
            p = BigInteger.probablePrime(KEY_SIZE / 2, random);
            q = BigInteger.probablePrime(KEY_SIZE / 2, random);
        } while (p.equals(q));

        // n = p × q
        BigInteger n = p.multiply(q);

        // φ(n) = (p - 1)(q - 1)
        BigInteger phi = p.subtract(BigInteger.ONE)
                .multiply(q.subtract(BigInteger.ONE));

        // e = 65537
        BigInteger e = BigInteger.valueOf(65537);

        // Check gcd(e, φ(n)) = 1
        if (!e.gcd(phi).equals(BigInteger.ONE)) {
            throw new IllegalStateException(
                    "Cannot generate valid RSA key pair"
            );
        }

        // d = e^-1 mod φ(n)
        BigInteger d = e.modInverse(phi);

        return new RSAKeyPair(
                n,
                e,
                d
        );
    }

    public record RSAKeyPair(
            BigInteger modulus,
            BigInteger publicExponent,
            BigInteger privateExponent
    ) {}
}
