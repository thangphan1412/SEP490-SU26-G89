package com.fpt.backend.dto.request.project;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProjectListRequest(
        @Size(max = 255, message = "Search text must not exceed 255 characters")
        String search,

        @Size(max = 50, message = "Project status must not exceed 50 characters")
        String status,

        boolean viewOnlyYourProjects,

        @PositiveOrZero(message = "Page index must be zero or greater")
        int page
) {
}
