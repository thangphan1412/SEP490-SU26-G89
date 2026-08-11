package com.fpt.backend.dto.response.contract;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ContractWorkflowStepRuntimeResponse(
        UUID id,
        UUID workflowStepId,
        Integer stepOrder,
        String stepName,
        String actionType,
        String requiredRoleCode,
        List<String> requiredPermissionCodes,
        boolean required,
        boolean canReject,
        UUID assignedUserId,
        String assignedUserName,
        String status,
        LocalDateTime activatedAt,
        LocalDateTime completedAt,
        String comment,
        boolean currentUserAssigned
) {
}
