package com.fpt.backend.dto.response.phase;

import com.fpt.backend.enums.PhaseStatus;

import java.time.LocalDate;
import java.util.UUID;

public record PhaseListItemResponse(
        UUID id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        PhaseStatus status,
        Double progress,
        UUID projectId,
        String projectCode,
        String projectName
) {
}
