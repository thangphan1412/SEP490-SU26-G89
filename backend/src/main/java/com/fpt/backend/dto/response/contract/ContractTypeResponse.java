package com.fpt.backend.dto.response.contract;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContractTypeResponse(
        UUID id,
        String contractTypeCode,
        String contractTypeName,
        String description,
        Integer validityDays,
        String category,
        String status,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long templateCount,
        long contractCount,
        ContractWorkflowDefinitionResponse activeWorkflow
) {
}
