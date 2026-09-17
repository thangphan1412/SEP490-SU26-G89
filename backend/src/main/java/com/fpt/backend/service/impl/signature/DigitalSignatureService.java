package com.fpt.backend.service.impl.signature;

import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.repository.signature.UserKeysRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DigitalSignatureService {

    private final UserKeysRepository userKeysRepository;
    public SignatureResult prepareSigning(
            byte[] document,
            UUID userId,
            String keyCode
    ) throws Exception {

        if (document == null || document.length == 0) {
            throw new IllegalArgumentException("Document is empty");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        UserKeys userKeys = userKeysRepository
                .findByUserIdAndKeyCode(userId, keyCode)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User RSA key not found"
                        )
                );

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(document);

        String documentHash =
                Base64.getEncoder()
                        .encodeToString(hash);

        return new SignatureResult(
                documentHash,
                userKeys.getPublicKey(),
                userKeys.getKeyCode(),
                userKeys.getKeyAlgorithm().name()
        );
    }
    public boolean verifyDocumentHash(
            byte[] document,
            String documentHash
    ) throws Exception {

        if (document == null || document.length == 0) {
            throw new IllegalArgumentException(
                    "Document is empty"
            );
        }

        if (documentHash == null || documentHash.isBlank()) {
            return false;
        }

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(document);

        String currentDocumentHash =
                Base64.getEncoder()
                        .encodeToString(hash);

        return currentDocumentHash.equals(documentHash);
    }

    public record SignatureResult(
            String documentHash,
            String publicKey,
            String keyCode,
            String keyAlgorithm
    ) {
    }
}