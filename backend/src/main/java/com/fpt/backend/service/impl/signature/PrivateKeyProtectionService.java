package com.fpt.backend.service.impl.signature;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class PrivateKeyProtectionService {

    private static final String PREFIX = "enc:v1:";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;
    private final SecretKeySpec encryptionKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public PrivateKeyProtectionService(
            @Value("${signature.private-key-secret:${jwt.secret}}") String secret
    ) throws Exception {
        byte[] key = MessageDigest.getInstance("SHA-256")
                .digest(secret.getBytes(StandardCharsets.UTF_8));
        this.encryptionKey = new SecretKeySpec(key, "AES");
    }

    public String encrypt(String privateKey) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(privateKey.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to protect private key", exception);
        }
    }

    public String decrypt(String protectedPrivateKey) {
        if (protectedPrivateKey == null || protectedPrivateKey.isBlank()) {
            throw new IllegalArgumentException("Private key is empty");
        }
        // Backward compatibility for keys generated before encryption was introduced.
        if (!protectedPrivateKey.startsWith(PREFIX)) {
            return protectedPrivateKey;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(protectedPrivateKey.substring(PREFIX.length()));
            byte[] iv = Arrays.copyOfRange(payload, 0, IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(payload, IV_LENGTH, payload.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new GCMParameterSpec(TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to unlock private key", exception);
        }
    }

    public boolean isProtected(String privateKey) {
        return privateKey != null && privateKey.startsWith(PREFIX);
    }
}
