package com.fpt.backend.dto.response.permission;

import java.time.LocalDateTime;

public record PermissionDetailResponse(
        int id,
        String permissionName,
        String permissionCode,
        String permissionModule,
        String permissionDescription,
        Boolean status,
        Integer projectId,
        String projectCode,
        String projectName,
        Integer roleId,
        String roleName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
