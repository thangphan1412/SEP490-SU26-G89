package com.fpt.backend.dto.request.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record TaskCreateRequest(
        @NotBlank(message = "Task title is required")
        @Size(max = 255, message = "Task title must not exceed 255 characters")
        String title,

        @NotNull(message = "Task start date is required")
        LocalDate startDate,

        @NotNull(message = "Task end date is required")
        LocalDate endDate,

        UUID assignedToId
) {
}
