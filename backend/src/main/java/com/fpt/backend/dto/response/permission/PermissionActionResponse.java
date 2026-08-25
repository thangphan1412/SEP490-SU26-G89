package com.fpt.backend.dto.response.permission;

import java.util.UUID;

public record PermissionActionResponse(
        UUID id,
        String actionCode,
        String actionName,
        String resourceCode,
        String description,
        Integer displayOrder
) {
}
