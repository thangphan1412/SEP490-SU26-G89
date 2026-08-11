package com.fpt.backend.dto.response.contract;

import java.util.List;
import java.util.UUID;

public record ContractAccessResponse(
        UUID projectId,
        UUID currentUserId,
        boolean projectCreator,
        boolean projectMember,
        List<String> allowedActions,
        List<String> fullScopeActions,
        String workScope,
        boolean currentUserOwner,
        boolean currentUserWorkflowParticipant
) {
}
