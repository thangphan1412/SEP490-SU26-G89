package com.fpt.backend.util;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.SecureRandom;

@Component
public class PrimeGenerator {
    public String primeGenerator() {
        SecureRandom random = new SecureRandom();
        BigInteger prime = BigInteger.probablePrime(128, random);

        return prime.toString(16);
    }
}
