package com.fpt.backend.dto.response.permission;

import java.time.LocalDateTime;
import java.util.UUID;

public record PermissionListItemResponse(
        UUID id,
        String permissionName,
        String permissionCode,
        String permissionDescription,
        Boolean status,
        UUID projectId,
        String projectCode,
        String projectName,
        LocalDateTime createdAt,
        boolean canManage
) {
}
