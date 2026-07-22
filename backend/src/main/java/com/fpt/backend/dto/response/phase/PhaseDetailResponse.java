package com.fpt.backend.dto.response.phase;

import java.time.LocalDate;
import java.util.List;

public record PhaseDetailResponse(
        int id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Double progress,
        int projectId,
        String projectCode,
        String projectName,
        List<PhaseTaskResponse> tasks,
        List<PhaseDeliverableResponse> deliverables,
        List<PhaseContractResponse> contracts
) {
}
