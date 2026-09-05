package com.fpt.backend.dto.request.contract;

import java.util.UUID;

public record ContractWorkflowStepRequest(
        Integer stepOrder,
        String stepName,
        String actionType,
        String requiredRoleCode,
        UUID requiredDepartmentId,
        Boolean required,
        Boolean canReject
) {
}
