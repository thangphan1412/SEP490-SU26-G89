package com.fpt.backend.dto.response.contract;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ContractWorkflowDefinitionResponse(
        UUID id,
        Integer versionNumber,
        String workflowName,
        boolean active,
        String createdBy,
        LocalDateTime createdAt,
        List<ContractWorkflowStepDefinitionResponse> steps
) {
}
