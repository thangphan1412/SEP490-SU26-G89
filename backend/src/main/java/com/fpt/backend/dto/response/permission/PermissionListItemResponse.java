package com.fpt.backend.dto.response.permission;

public record PermissionListItemResponse(
        int id,
        String permissionName,
        String permissionCode,
        String permissionModule,
        Integer projectId,
        String projectName
) {
}
