package com.fpt.backend.util;

import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;

public class CalculateRSA {
    public void calculateRSA() {
        PrimeGenerator primeGenerators = new PrimeGenerator();
        String prime1 = primeGenerators.primeGenerator();
        String prime2 = primeGenerators.primeGenerator();
        BigInteger p = new BigInteger(prime1, 16);
        BigInteger q = new BigInteger(prime2, 16);
        BigInteger modulus = p.multiply(q);
        System.out.println(p);
        System.out.println(q);
        System.out.println(modulus);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger publicExponent = BigInteger.valueOf(65537);
        if(!publicExponent.gcd(phi).equals(BigInteger.ONE) ){
            throw new IllegalArgumentException("e not valid");
        }
        BigInteger privateExponent = publicExponent.modInverse(phi);

    }
}
