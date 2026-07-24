package com.fpt.backend.dto.response.phase;

import java.time.LocalDate;

public record PhaseTaskResponse(
        int id,
        String title,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        Double progress,
        Integer assignedToId,
        String assignedToName,
        String assignedToEmail
) {
}
