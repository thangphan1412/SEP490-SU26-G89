package com.fpt.backend.dto.response.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectListItemResponse {
    private UUID id;
    private String projectCode;
    private String projectName;
    private String projectDescription;
    private String projectStatus;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;
    private String projectCreatedBy;
    private String projectCreatedAt;
    private boolean canView;
    private boolean canApprove;
}
