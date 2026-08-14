package com.fpt.backend.dto.response.phase;

import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.enums.PhaseStatus;
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
public class PhaseDetailResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private PhaseStatus status;
    private Double progress;
    private UUID projectId;
    private String projectCode;
    private String projectName;
    private List<PhaseTaskResponse> tasks;
    private List<PhaseDeliverableResponse> deliverables;
    private List<PhaseContractResponse> contracts;
    private ProjectAccessResponse currentUserAccess;
}
