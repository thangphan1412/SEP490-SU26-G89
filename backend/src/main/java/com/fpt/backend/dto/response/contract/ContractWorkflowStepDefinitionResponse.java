package com.fpt.backend.dto.response.contract;

import java.util.List;
import java.util.UUID;

public record ContractWorkflowStepDefinitionResponse(
        UUID id,
        Integer stepOrder,
        String stepName,
        String actionType,
        String requiredRoleCode,
        UUID requiredDepartmentId,
        String requiredDepartmentCode,
        String requiredDepartmentName,
        List<String> requiredPermissionCodes,
        boolean required,
        boolean canReject
) {
}
