package com.fpt.backend.dto.response.phase;

import java.time.LocalDate;

public record PhaseListItemResponse(
        int id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Double progress,
        int projectId,
        String projectCode,
        String projectName
) {
}
