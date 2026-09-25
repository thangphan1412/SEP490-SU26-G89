package com.fpt.backend.dto.request.contract;

import java.time.LocalDate;
import java.util.UUID;

public record ContractTransitionRequest(
        String action,
        String actorName,
        String actorRole,
        String comment,
        String signatureValue,
        UUID electronicSignatureId,
        String keyCode,
        // Chỉ dùng cho EXTEND_SIGNING_DEADLINE
        LocalDate signingDeadline
) {
}
