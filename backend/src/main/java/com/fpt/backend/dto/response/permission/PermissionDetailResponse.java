package com.fpt.backend.dto.response.permission;

public record PermissionDetailResponse(
        int id,
        String permissionName,
        String permissionCode,
        String permissionModule,
        Integer projectId,
        String projectName
) {
}
