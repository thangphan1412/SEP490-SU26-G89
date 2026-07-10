package com.fpt.backend.dto.response.permission;

public record PermissionProjectResponse(
        int id,
        String projectCode,
        String projectName
) {
}
