package com.fpt.backend.dto.request.project;

import java.time.LocalDate;

public record ProjectPhaseRequest(
        Integer id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Double progress
) {
}
