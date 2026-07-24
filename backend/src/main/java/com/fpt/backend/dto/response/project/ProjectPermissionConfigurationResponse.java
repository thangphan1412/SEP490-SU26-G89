package com.fpt.backend.dto.response.project;

import java.util.List;

public record ProjectPermissionConfigurationResponse(
        int permissionId,
        String permissionName,
        String permissionCode,
        String permissionDescription,
        Boolean status,
        List<String> allowedActions,
        String workScope
) {
}
