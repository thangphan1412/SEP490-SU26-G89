package com.fpt.backend.service.interfaces.permission;

import com.fpt.backend.dto.response.permission.PermissionActionResponse;
import com.fpt.backend.entity.Permissions;

import java.util.List;

public interface PermissionActionService {
    void configurePermission(
            Permissions permission,
            List<String> allowedActionCodes,
            String workScope
    );

    void configureFullAccess(Permissions permission);

    List<String> getAllowedActionCodes(Permissions permission);

    List<PermissionActionResponse> getActionDetails(Permissions permission);

    String getWorkScope(Permissions permission);

    List<PermissionActionResponse> getAvailableActions();
}
