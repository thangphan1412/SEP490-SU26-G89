package com.fpt.backend.dto.response.task;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TaskItemResponse(
        UUID id,
        String title,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        UUID assignedToId,
        String assignedToName,
        String assignedToEmail,
        List<TaskContractResponse> contracts
) {
}
