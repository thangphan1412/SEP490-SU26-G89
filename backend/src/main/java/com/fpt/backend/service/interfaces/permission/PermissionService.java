package com.fpt.backend.service.interfaces.permission;

import com.fpt.backend.dto.request.permission.PermissionListRequest;
import com.fpt.backend.dto.request.permission.PermissionRequest;
import com.fpt.backend.dto.response.permission.PermissionActionResponse;
import com.fpt.backend.dto.response.permission.PermissionDetailResponse;
import com.fpt.backend.dto.response.permission.PermissionListResponse;
import com.fpt.backend.dto.response.permission.PermissionProjectResponse;

import java.util.List;
import java.util.UUID;

public interface PermissionService {
    PermissionListResponse getPermissions(PermissionListRequest request);

    PermissionDetailResponse getPermissionById(UUID id);

    PermissionDetailResponse createPermission(PermissionRequest request);

    PermissionDetailResponse updatePermission(UUID id, PermissionRequest request);

    void deletePermission(UUID id);

    List<PermissionProjectResponse> getProjectsForPermissionSelection();

    List<PermissionActionResponse> getAvailableActions();
}
