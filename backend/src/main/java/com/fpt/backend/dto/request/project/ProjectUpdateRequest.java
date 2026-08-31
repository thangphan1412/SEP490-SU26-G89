package com.fpt.backend.dto.request.project;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record ProjectUpdateRequest(
        @Pattern(regexp = ".*\\S.*", message = "Project name must not be blank")
        @Size(max = 50, message = "Project name must not exceed 50 characters")
        String projectName,

        @Pattern(
                regexp = "^PRJ-\\d{4}-[\\p{L}\\p{N}](?:[^\\r\\n]*[\\p{L}\\p{N}])?$",
                message = "Project code must follow format PRJ-yyyy-Project Name (e.g. PRJ-2026-Thời trang mùa đông)"
        )
        @Size(max = 50, message = "Project code must not exceed 50 characters")
        String projectCode,

        LocalDate projectStartDate,

        LocalDate projectEndDate,

        @Size(max = 255, message = "Project description must not exceed 255 characters")
        String projectDescription,

        @Valid
        List<@NotNull(message = "Phase information is required") ProjectPhaseRequest> phases,

        @Valid
        List<@NotNull(message = "Member information is required") ProjectMemberRequest> members
) {
}
