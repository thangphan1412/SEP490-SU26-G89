package com.fpt.backend.dto.request.contract;

import java.util.UUID;

public record ContractWorkflowAssigneeRequest(
        UUID workflowStepId,
        UUID userId
) {
}
