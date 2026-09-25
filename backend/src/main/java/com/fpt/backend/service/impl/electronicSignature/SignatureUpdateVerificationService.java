package com.fpt.backend.service.impl.electronicSignature;

import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.enums.KeyStatus;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.electronicSignature.ElectronicSignatureRepository;
import com.fpt.backend.repository.signature.UserKeysRepository;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignatureUpdateVerificationService {
    private final ElectronicSignatureRepository signatures;
    private final UserKeysRepository keys;
    private final CurrentUser currentUser;
    private final RedisTemplate<String, String> redisTemplate;

    public Challenge create(UUID signatureId, String keyCode) {
        UUID userId = currentUser.getCurrentUser().getId();
        signatures.findOwnedById(signatureId, userId)
                .orElseThrow(() -> new BadHttpException("Signature not found or access denied"));
        UserKeys key = requireKey(userId, keyCode);
        UUID challengeId = UUID.randomUUID();
        String message = "electronic-signature-update\n" + userId + "\n" + signatureId
                + "\n" + key.getKeyCode() + "\n" + challengeId;
        redisTemplate.opsForValue().set("signature-update:" + userId + ":" + challengeId,
                message, Duration.ofMinutes(5));
        return new Challenge(challengeId, message, key.getPublicKey(), key.getKeyCode(),
                Instant.now().plusSeconds(300).toString());
    }

    public void verify(UUID signatureId, UUID challengeId, String signatureValue) {
        UUID userId = currentUser.getCurrentUser().getId();
        if (challengeId == null || signatureValue == null || signatureValue.isBlank()) {
            throw new BadHttpException("Verify your public and private keys before saving changes");
        }
        String message = redisTemplate.opsForValue().getAndDelete("signature-update:" + userId + ":" + challengeId);
        if (message == null) {
            throw new BadHttpException("Key verification has expired or was already used. Please verify again");
        }
        String[] parts = message.split("\n");
        if (parts.length != 5 || !parts[0].equals("electronic-signature-update")
                || !parts[1].equals(userId.toString()) || !parts[2].equals(signatureId.toString())
                || !parts[4].equals(challengeId.toString())) {
            throw new BadHttpException("Key verification does not belong to this signature");
        }
        UserKeys key = requireKey(userId, parts[3]);
        try {
            var publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(
                    Base64.getDecoder().decode(key.getPublicKey())));
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(message.getBytes(StandardCharsets.UTF_8));
            if (!verifier.verify(Base64.getDecoder().decode(signatureValue))) {
                throw new BadHttpException("The private key does not match your registered public key");
            }
        } catch (BadHttpException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BadHttpException("Unable to verify the key pair. Check your key and try again");
        }
    }

    private UserKeys requireKey(UUID userId, String keyCode) {
        if (keyCode == null || !keyCode.matches("\\d{6}")) {
            throw new BadHttpException("Public key code must contain exactly 6 digits");
        }
        UserKeys key = keys.findByUserIdAndKeyCode(userId, keyCode)
                .orElseThrow(() -> new BadHttpException("No registered public key matches this code"));
        if (key.getKeyStatus() == KeyStatus.REVOKED || key.getKeyStatus() == KeyStatus.INACTIVE) {
            throw new BadHttpException("This signing key is inactive or revoked");
        }
        return key;
    }

    public record Challenge(UUID challengeId, String challenge, String publicKey, String keyCode, String expiresAt) {}
}
