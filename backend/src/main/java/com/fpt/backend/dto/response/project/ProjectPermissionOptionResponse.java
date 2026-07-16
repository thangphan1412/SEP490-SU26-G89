package com.fpt.backend.dto.response.project;

public record ProjectPermissionOptionResponse(
        Integer id,
        String value,
        String permissionName,
        String permissionCode,
        String permissionDescription,
        Boolean status
) {
}
