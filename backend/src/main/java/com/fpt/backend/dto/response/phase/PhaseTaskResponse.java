package com.fpt.backend.dto.response.phase;

import java.time.LocalDate;
import java.util.UUID;

public record PhaseTaskResponse(
        UUID id,
        String title,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        UUID assignedToId,
        String assignedToName,
        String assignedToEmail
) {
}
