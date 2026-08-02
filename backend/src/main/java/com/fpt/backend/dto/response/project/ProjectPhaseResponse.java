package com.fpt.backend.dto.response.project;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectPhaseResponse(
        UUID id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Double progress
) {
}
