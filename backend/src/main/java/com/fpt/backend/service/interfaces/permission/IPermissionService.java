package com.fpt.backend.service.interfaces.permission;

import com.fpt.backend.dto.request.permission.PermissionListRequest;
import com.fpt.backend.dto.request.permission.PermissionRequest;
import com.fpt.backend.dto.response.permission.PermissionActionResponse;
import com.fpt.backend.dto.response.permission.PermissionDetailResponse;
import com.fpt.backend.dto.response.permission.PermissionListResponse;
import com.fpt.backend.dto.response.permission.PermissionProjectResponse;

import java.util.List;
import java.util.UUID;

public interface IPermissionService {
    // Tìm kiếm và phân trang danh sách quyền theo request.
    PermissionListResponse getPermissions(PermissionListRequest request);

    // Lấy chi tiết quyền theo mã định danh.
    PermissionDetailResponse getPermissionById(UUID id);

    // Tạo quyền mới từ request.
    PermissionDetailResponse createPermission(PermissionRequest request);

    // Cập nhật quyền hiện có từ request.
    PermissionDetailResponse updatePermission(UUID id, PermissionRequest request);

    // Xóa quyền theo mã định danh.
    void deletePermission(UUID id);

    // Lấy các dự án có thể chọn khi cấu hình quyền.
    List<PermissionProjectResponse> getProjectsForPermissionSelection();

    // Lấy toàn bộ action khả dụng trong permission catalog.
    List<PermissionActionResponse> getAvailableActions();
}
