package com.fpt.backend.dto.request.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectPhaseRequest(
        UUID id,

        @NotBlank(message = "Phase title is required")
        @Size(max = 150, message = "Phase title must not exceed 150 characters")
        String title,

        @Size(max = 500, message = "Phase description must not exceed 500 characters")
        String description,

        @NotNull(message = "Phase start date is required")
        LocalDate startDate,

        @NotNull(message = "Phase end date is required")
        LocalDate endDate
) {
}
