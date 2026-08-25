package com.fpt.backend.dto.response.project;

import java.util.UUID;

public record ProjectPermissionOptionResponse(
        UUID id,
        String permissionName,
        String permissionCode,
        String permissionDescription,
        Boolean status
) {
}
