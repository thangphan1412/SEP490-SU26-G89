package com.fpt.backend.dto.response.project;

import java.time.LocalDate;
import java.util.List;

public record ProjectDetailResponse(
        int id,
        String projectCode,
        String projectName,
        String projectDescription,
        String projectStatus,
        LocalDate projectStartDate,
        LocalDate projectEndDate,
        String projectCreatedBy,
        String projectCreatedAt,
        List<ProjectUserResponse> users,
        List<ProjectContractResponse> contracts
) {
}
