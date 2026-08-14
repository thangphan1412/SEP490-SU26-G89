package com.fpt.backend.dto.response.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectDetailResponse {
    private UUID id;
    private String projectCode;
    private String projectName;
    private String projectDescription;
    private String projectStatus;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;
    private String projectCreatedBy;
    private String projectCreatedAt;
    private List<ProjectPhaseResponse> phases;
    private List<ProjectUserResponse> users;
    private List<ProjectPermissionOptionResponse> availablePermissions;
    private List<ProjectContractResponse> contracts;
    private ProjectAccessResponse currentUserAccess;
}
