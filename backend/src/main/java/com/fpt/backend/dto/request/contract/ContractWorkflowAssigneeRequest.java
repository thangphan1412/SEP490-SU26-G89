package com.fpt.backend.dto.request.contract;

import java.util.UUID;

public record ContractWorkflowAssigneeRequest(
        String workflowStepId,
        Integer stepOrder,
        UUID userId
) {
}
