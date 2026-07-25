package com.fpt.backend.dto.response.permission;

import java.util.UUID;

public record PermissionProjectResponse(
        UUID id,
        String projectCode,
        String projectName
) {
}
