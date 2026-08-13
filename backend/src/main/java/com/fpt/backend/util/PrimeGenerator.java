package com.fpt.backend.util;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.SecureRandom;

@Component
public class PrimeGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    public BigInteger generatePrime(int bitLength) {
        return BigInteger.probablePrime(
                bitLength,
                secureRandom
        );
    }
}
