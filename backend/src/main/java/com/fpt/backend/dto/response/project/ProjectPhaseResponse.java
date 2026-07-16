package com.fpt.backend.dto.response.project;

import java.time.LocalDate;

public record ProjectPhaseResponse(
        int id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Double progress
) {
}
