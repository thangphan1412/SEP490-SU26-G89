package com.fpt.backend.dto.response.permission;

import java.time.LocalDateTime;
import java.util.UUID;

public record PermissionDetailResponse(
        UUID id,
        String permissionName,
        String permissionCode,
        String permissionModule,
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
