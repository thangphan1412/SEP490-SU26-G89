package com.fpt.backend.dto.response.phase;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PhaseDetailResponse(
        UUID id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Double progress,
        UUID projectId,
        String projectCode,
        String projectName,
        List<PhaseTaskResponse> tasks,
        List<PhaseDeliverableResponse> deliverables,
        List<PhaseContractResponse> contracts
) {
}
