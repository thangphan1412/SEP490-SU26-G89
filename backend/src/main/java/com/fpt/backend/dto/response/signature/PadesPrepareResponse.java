package com.fpt.backend.dto.response.signature;

import java.util.UUID;

public record PadesPrepareResponse(
        UUID sessionId,
        String documentHash,
        String contentToSign,
        String hashAlgorithm,
        String signatureAlgorithm,
        String keyCode
) {
}
