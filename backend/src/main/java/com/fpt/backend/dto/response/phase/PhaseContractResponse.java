package com.fpt.backend.dto.response.phase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PhaseContractResponse(
        UUID id,
        String contractNumber,
        String contractTitle,
        String contractStatus,
        LocalDate effectiveDate,
        LocalDate expirationDate,
        LocalDateTime linkedAt
) {
}
