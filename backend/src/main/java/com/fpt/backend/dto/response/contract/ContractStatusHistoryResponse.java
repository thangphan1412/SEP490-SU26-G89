package com.fpt.backend.dto.response.contract;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContractStatusHistoryResponse(
        UUID id,
        String fromStatus,
        String toStatus,
        String action,
        String actorName,
        String actorRole,
        String comment,
        Boolean signerAgeVerified,
        LocalDateTime changedAt
) {
}
