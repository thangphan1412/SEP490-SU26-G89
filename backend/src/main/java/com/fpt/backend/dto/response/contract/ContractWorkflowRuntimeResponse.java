package com.fpt.backend.dto.response.contract;

import java.util.List;
import java.util.UUID;

public record ContractWorkflowRuntimeResponse(
        UUID workflowVersionId,
        Integer workflowVersionNumber,
        String workflowName,
        UUID currentStepId,
        String currentStepName,
        String currentStepActionType,
        UUID currentAssignedUserId,
        String currentAssignedUserName,
        List<String> availableActions,
        List<ContractWorkflowStepRuntimeResponse> steps
) {
}
