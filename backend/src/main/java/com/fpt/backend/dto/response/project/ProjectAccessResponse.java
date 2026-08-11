package com.fpt.backend.dto.response.project;

import java.util.List;
import java.util.UUID;

public record ProjectAccessResponse(
        UUID projectId,
        UUID currentUserId,
        boolean projectMember,
        List<String> allowedActions,
        String workScope
) {
    public boolean projectCreator() {
        return false;
    }

    public List<String> fullScopeActions() {
        if ("FULL".equalsIgnoreCase(workScope) && allowedActions != null) {
            return allowedActions;
        }

        return List.of();
    }
}
