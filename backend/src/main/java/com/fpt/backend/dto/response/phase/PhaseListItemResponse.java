package com.fpt.backend.dto.response.phase;

import java.time.LocalDate;
import java.util.UUID;

public record PhaseListItemResponse(
        UUID id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Double progress,
        UUID projectId,
        String projectCode,
        String projectName
) {
}
