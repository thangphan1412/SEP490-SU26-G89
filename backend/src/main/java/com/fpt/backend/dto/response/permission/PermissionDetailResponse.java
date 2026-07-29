package com.fpt.backend.dto.response.permission;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PermissionDetailResponse(
        UUID id,
        String permissionName,
        String permissionCode,
        String permissionModule,
        List<String> allowedActions,
        String workScope,
        String permissionDescription,
        Boolean status,
        UUID projectId,
        String projectCode,
        String projectName,
        UUID roleId,
        String roleName,
        LocalDateTime createdAt
) {
}
