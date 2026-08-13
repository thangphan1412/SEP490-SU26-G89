package com.fpt.backend.util;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.SecureRandom;

@Component
public class PrimeGenerator {
    public BigInteger primeGenerator() {
        SecureRandom random = new SecureRandom();

        return BigInteger.probablePrime(1024, random);
    }
}
