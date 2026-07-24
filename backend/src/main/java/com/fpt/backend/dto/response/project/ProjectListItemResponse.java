package com.fpt.backend.dto.response.project;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectListItemResponse(
        UUID id,
        String projectCode,
        String projectName,
        String projectDescription,
        String projectStatus,
        LocalDate projectStartDate,
        LocalDate projectEndDate,
        String projectCreatedBy,
        String projectCreatedAt
) {
}
