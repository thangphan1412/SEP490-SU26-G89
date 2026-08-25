package com.fpt.backend.dto.response.signature;

import java.util.UUID;

public record SignatureVerificationResponse(
        UUID signatureId,
        UUID contractId,
        UUID signerId,
        String documentHash,
        boolean valid
) {
}
