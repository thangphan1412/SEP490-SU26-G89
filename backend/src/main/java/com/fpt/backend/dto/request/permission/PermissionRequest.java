package com.fpt.backend.dto.request.permission;

public record PermissionRequest(
        String permissionName,
        String permissionCode,
        String permissionModule,
        Integer projectId
) {
}
