package com.fpt.backend.dto.response.project;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record ProjectAccessResponse(
        UUID projectId,
        UUID currentUserId,
        @JsonProperty("isProjectCreator") boolean isProjectCreator,
        @JsonProperty("isProjectMember") boolean isProjectMember,
        @JsonProperty("isExecutiveViewer") boolean isExecutiveViewer,
        List<String> allowedActions,
        List<String> fullScopeActions,
        String workScope
) {
    public UUID getProjectId() {
        return projectId;
    }

    public UUID getCurrentUserId() {
        return currentUserId;
    }

    public List<String> getAllowedActions() {
        return allowedActions;
    }

    public List<String> getFullScopeActions() {
        return fullScopeActions;
    }

    public String getWorkScope() {
        return workScope;
    }
}
