package com.fpt.backend.dto.response.project;

import com.fpt.backend.enums.PhaseStatus;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectPhaseResponse(
        UUID id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        PhaseStatus status,
        Double progress
) {
}
