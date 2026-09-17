package com.fpt.backend.dto.request.signature;

import java.util.UUID;

public record PadesCompleteRequest(
        UUID sessionId,
        String signatureValue
) {
}