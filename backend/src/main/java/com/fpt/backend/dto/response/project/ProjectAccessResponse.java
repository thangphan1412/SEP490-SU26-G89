package com.fpt.backend.dto.response.project;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ProjectAccessResponse(
        UUID projectId,
        UUID currentUserId,
        boolean projectMember,
        boolean canViewAllProjectData,
        List<String> allowedActions,
        String workScope
) {
    public boolean projectCreator() {
        return false;
    }

    public List<String> fullScopeActions() {
        if (allowedActions == null) {
            return List.of();
        }

        if ("FULL".equalsIgnoreCase(workScope)) {
            return allowedActions;
        }

        if (!canViewAllProjectData) {
            return List.of();
        }

        List<String> viewActions = new ArrayList<>();

        for (String actionCode : allowedActions) {
            if (actionCode != null && actionCode.startsWith("VIEW_")) {
                viewActions.add(actionCode);
            }
        }

        return viewActions;
    }
}
