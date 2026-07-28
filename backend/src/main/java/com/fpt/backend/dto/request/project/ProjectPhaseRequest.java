package com.fpt.backend.dto.request.project;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectPhaseRequest(
        UUID id,
        String title,
        String description,
        LocalDate endDate,
        String status
) {
}
