package com.fpt.backend.dto.response.phase;

import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.enums.PhaseStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PhaseDetailResponse(
        UUID id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        PhaseStatus status,
        Double progress,
        UUID projectId,
        String projectCode,
        String projectName,
        List<PhaseTaskResponse> tasks,
        List<PhaseDeliverableResponse> deliverables,
        List<PhaseContractResponse> contracts,
        ProjectAccessResponse currentUserAccess
) {
}
