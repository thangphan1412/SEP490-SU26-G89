package com.fpt.backend.dto.response.project;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectDetailResponse(
        UUID id,
        String projectCode,
        String projectName,
        String projectDescription,
        String projectStatus,
        LocalDate projectStartDate,
        LocalDate projectEndDate,
        String projectCreatedBy,
        String projectCreatedAt,
        List<ProjectPhaseResponse> phases,
        List<ProjectUserResponse> users,
        List<ProjectPermissionOptionResponse> availablePermissions,
        List<ProjectContractResponse> contracts
) {
}
