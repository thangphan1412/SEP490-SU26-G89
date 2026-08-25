package com.fpt.backend.dto.response.project;

import java.util.List;
import java.util.UUID;

public record ProjectPermissionConfigurationResponse(
        UUID permissionId,
        String permissionName,
        String permissionCode,
        String permissionDescription,
        Boolean status,
        List<String> allowedActions,
        String workScope
) {
}
