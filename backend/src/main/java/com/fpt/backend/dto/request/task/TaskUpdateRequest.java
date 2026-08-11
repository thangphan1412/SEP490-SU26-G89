package com.fpt.backend.dto.request.task;

import com.fpt.backend.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record TaskUpdateRequest(
        @NotBlank(message = "Task title is required")
        @Size(max = 255, message = "Task title must not exceed 255 characters")
        String title,

        @NotNull(message = "Task start date is required")
        LocalDate startDate,

        @NotNull(message = "Task end date is required")
        LocalDate endDate,

        @NotNull(message = "Task status is required")
        TaskStatus status,

        UUID assignedToId
) {
}
