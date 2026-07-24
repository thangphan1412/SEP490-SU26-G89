package com.fpt.backend.dto.request.permission;

public record PermissionRequest(
        String permissionName,
        String permissionCode,
        String permissionDescription,
        Boolean status,
        Integer projectId,
        Integer roleId
) {
}
