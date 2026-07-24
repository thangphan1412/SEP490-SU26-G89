package com.fpt.backend.dto.response.contract;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ContractResponse(
        UUID id,
        UUID projectId,
        String projectName,
        String contractNumber,
        String contractTitle,
        String contractStatus,
        LocalDate effectiveDate,
        LocalDate expirationDate,
        String contractCreatedBy,
        LocalDateTime contractCreatedAt
) {
}
