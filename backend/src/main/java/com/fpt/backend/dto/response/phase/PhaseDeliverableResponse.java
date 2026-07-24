package com.fpt.backend.dto.response.phase;

import java.time.LocalDate;

public record PhaseDeliverableResponse(
        int id,
        String title,
        String description,
        LocalDate dueDate,
        String status
) {
}
