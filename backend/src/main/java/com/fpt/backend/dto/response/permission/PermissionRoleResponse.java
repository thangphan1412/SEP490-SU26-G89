package com.fpt.backend.dto.response.permission;

import java.util.UUID;

public record PermissionRoleResponse(
        UUID id,
        String roleName
) {
}
