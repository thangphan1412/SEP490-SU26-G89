package com.fpt.backend.dto.request.contract;

public record ContractWorkflowStepRequest(
        Integer stepOrder,
        String stepName,
        String actionType,
        String requiredRoleCode,
        Boolean required,
        Boolean canReject
) {
}
