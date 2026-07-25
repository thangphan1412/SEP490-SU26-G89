package com.fpt.backend.dto.response.phase;

import java.time.LocalDate;
import java.util.UUID;

public record PhaseDeliverableResponse(
        UUID id,
        String title,
        String description,
        LocalDate dueDate,
        String status
) {
}
