package com.fpt.backend.dto.response.signature;

import java.time.LocalDateTime;

public record UserKeyInfoResponse(
        boolean available,
        String publicKey,
        String publicKeyFingerprint,
        String algorithm,
        long keySize,
        LocalDateTime createdAt
) {
}
