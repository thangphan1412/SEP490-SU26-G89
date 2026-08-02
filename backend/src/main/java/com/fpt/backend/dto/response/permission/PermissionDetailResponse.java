package com.fpt.backend.dto.response.permission;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PermissionDetailResponse(
        UUID id,
        String permissionName,
        String permissionCode,
        List<String> allowedActions,
        List<PermissionActionResponse> actionDetails,
        String workScope,
        String permissionDescription,
        Boolean status,
        UUID projectId,
        String projectCode,
        String projectName,
        LocalDateTime createdAt
) {
}
