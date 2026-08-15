package com.fpt.backend.service.impl.signature;

import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.enums.KeyAlgorithm;
import com.fpt.backend.repository.signature.UserKeysRepository;
import com.fpt.backend.util.RSAKeyConverter;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DigitalSignatureServiceTest {

    @Test
    void encryptedPrivateKeySignsAndPublicKeyVerifiesPdfHash() throws Exception {
        SecureRandom random = new SecureRandom();
        BigInteger e = BigInteger.valueOf(65537);
        BigInteger p;
        BigInteger q;
        BigInteger phi;
        do {
            p = BigInteger.probablePrime(512, random);
            q = BigInteger.probablePrime(512, random);
            phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        } while (!e.gcd(phi).equals(BigInteger.ONE));

        BigInteger n = p.multiply(q);
        BigInteger d = e.modInverse(phi);
        String rawPrivateKey = RSAKeyConverter.encode(n, d);
        PrivateKeyProtectionService protection = new PrivateKeyProtectionService(
                "test-only-secret-that-is-not-used-in-production"
        );
        String encryptedPrivateKey = protection.encrypt(rawPrivateKey);
        assertNotEquals(rawPrivateKey, encryptedPrivateKey);
        assertEquals(rawPrivateKey, protection.decrypt(encryptedPrivateKey));

        UUID userId = UUID.randomUUID();
        UserKeys key = UserKeys.builder()
                .keyAlgorithm(KeyAlgorithm.RSA)
                .keySize(1024)
                .publicKey(RSAKeyConverter.encode(n, e))
                .privateKey(encryptedPrivateKey)
                .build();
        UserKeysRepository repository = mock(UserKeysRepository.class);
        when(repository.findByUserId(userId)).thenReturn(Optional.of(key));

        DigitalSignatureService signer = new DigitalSignatureService(repository, protection);
        DigitalSignatureVerificationService verifier = new DigitalSignatureVerificationService(repository);
        byte[] pdf = "%PDF-1.7 contract content".getBytes(StandardCharsets.UTF_8);

        DigitalSignatureService.SignatureResult result = signer.sign(pdf, userId);

        assertNotNull(result.documentHash());
        assertNotNull(result.signatureValue());
        assertTrue(verifier.verify(pdf, result.signatureValue(), userId));
        assertFalse(verifier.verify(
                "%PDF-1.7 changed content".getBytes(StandardCharsets.UTF_8),
                result.signatureValue(), userId
        ));
    }
}
