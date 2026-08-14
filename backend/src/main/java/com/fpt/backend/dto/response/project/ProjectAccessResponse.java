package com.fpt.backend.dto.response.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectAccessResponse {
    private UUID projectId;
    private UUID currentUserId;
    private boolean isProjectCreator;
    private boolean isProjectMember;
    private boolean isExecutiveViewer;
    private List<String> allowedActions;
    private List<String> fullScopeActions;
    private String workScope;

    @JsonProperty("isProjectCreator")
    public boolean isProjectCreator() {
        return isProjectCreator;
    }

    @JsonProperty("isProjectMember")
    public boolean isProjectMember() {
        return isProjectMember;
    }

    @JsonProperty("isExecutiveViewer")
    public boolean isExecutiveViewer() {
        return isExecutiveViewer;
    }
}
