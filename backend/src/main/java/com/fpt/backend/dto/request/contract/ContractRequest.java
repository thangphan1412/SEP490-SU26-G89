package com.fpt.backend.dto.request.contract;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ContractRequest(
        UUID projectId,
        String contractNumber,
        String contractTitle,
        String contractStatus,
        LocalDate effectiveDate,
        LocalDate expirationDate,
        String contractCreatedBy,
        LocalDateTime contractCreatedAt
) {
}
