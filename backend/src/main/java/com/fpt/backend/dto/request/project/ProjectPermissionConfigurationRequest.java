package com.fpt.backend.dto.request.project;

import java.util.List;

public record ProjectPermissionConfigurationRequest(
        List<String> allowedActions,
        String workScope
) {
}
