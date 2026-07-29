package com.fpt.backend.service.interfaces.permission;

import java.util.List;

public interface PermissionModuleService {
    String createModuleValue(
            List<String> allowedActions,
            String workScope
    );

    List<String> getAllowedActions(String permissionModule);

    String getWorkScope(String permissionModule);
}
