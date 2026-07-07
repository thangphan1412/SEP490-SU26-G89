package com.fpt.backend.dto.response.contract;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContractResponse(
        int id,
        Integer projectId,
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
