package com.fpt.backend.dto.request.permission;

import java.util.List;
import java.util.UUID;

public record PermissionRequest(
        String permissionName,
        String permissionCode,
        String permissionDescription,
        Boolean status,
        UUID projectId,
        UUID roleId,
        List<String> allowedActions,
        String workScope
) {
}
