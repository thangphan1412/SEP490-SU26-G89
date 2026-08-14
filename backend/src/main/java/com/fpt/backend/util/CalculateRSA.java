package com.fpt.backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigInteger;

@Component
@RequiredArgsConstructor
public class CalculateRSA {

    private static final int KEY_SIZE = 2048;

    private final PrimeGenerator primeGenerator;

    public RSAKeyPair generateKeyPair() {BigInteger p;BigInteger q;
        do {
            p = primeGenerator.generatePrime(KEY_SIZE / 2);
            q = primeGenerator.generatePrime(KEY_SIZE / 2);
        } while (p.equals(q));

        BigInteger n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE)
                        .multiply(q.subtract(BigInteger.ONE));
        BigInteger e = BigInteger.valueOf(65537);
        if (!e.gcd(phi).equals(BigInteger.ONE)) {
            throw new IllegalStateException("Invalid RSA public exponent");
        }
        BigInteger d = e.modInverse(phi);
        return new RSAKeyPair(n, e, d);
    }
    public record RSAKeyPair(
            BigInteger modulus,
            BigInteger publicExponent,
            BigInteger privateExponent
    ) {
    }
}