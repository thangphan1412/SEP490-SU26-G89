package com.fpt.backend.service.interfaces.permission;

import com.fpt.backend.dto.response.project.ProjectAccessResponse;

import java.util.List;
import java.util.UUID;

public interface PermissionAccessService {
    ProjectAccessResponse getCurrentUserAccess(UUID projectId);

    void requireAction(UUID projectId, String actionCode);

    boolean hasAction(
            ProjectAccessResponse access,
            String actionCode
    );

    boolean hasFullWorkScope(
            ProjectAccessResponse access,
            String actionCode
    );

    List<UUID> getCurrentUserProjectIdsWithAction(String actionCode);
}
