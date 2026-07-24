package com.fpt.backend.dto.response.phase;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PhaseContractResponse(
        int id,
        String contractNumber,
        String contractTitle,
        String contractStatus,
        LocalDate effectiveDate,
        LocalDate expirationDate,
        LocalDateTime linkedAt
) {
}
