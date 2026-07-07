package com.fpt.backend.dto.response.project;

import java.time.LocalDate;

public record ProjectListItemResponse(
        int id,
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
