package com.fpt.backend.dto.request.permission;

public record PermissionRequest(
        String permissionName,
        String permissionCode,
        String permissionModule,
        String permissionDescription,
        Boolean status,
        Integer projectId,
        Integer roleId
) {
}
