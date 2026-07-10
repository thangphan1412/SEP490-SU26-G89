package com.fpt.backend.service.interfaces.permission;

import com.fpt.backend.dto.request.permission.PermissionListRequest;
import com.fpt.backend.dto.request.permission.PermissionRequest;
import com.fpt.backend.dto.response.permission.PermissionDetailResponse;
import com.fpt.backend.dto.response.permission.PermissionListResponse;
import com.fpt.backend.dto.response.permission.PermissionProjectResponse;

import java.util.List;

public interface PermissionService {
    PermissionListResponse getPermissions(PermissionListRequest request);

    PermissionDetailResponse getPermissionById(int id);

    PermissionDetailResponse createPermission(PermissionRequest request);

    PermissionDetailResponse updatePermission(int id, PermissionRequest request);

    void deletePermission(int id);

    List<PermissionProjectResponse> getProjectsForPermissionSelection();
}
