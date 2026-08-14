package com.fpt.backend.controller.permissionController;

import com.fpt.backend.constant.ApiConstant;
import com.fpt.backend.dto.request.permission.PermissionListRequest;
import com.fpt.backend.dto.request.permission.PermissionRequest;
import com.fpt.backend.dto.response.permission.PermissionActionResponse;
import com.fpt.backend.dto.response.permission.PermissionDetailResponse;
import com.fpt.backend.dto.response.permission.PermissionListResponse;
import com.fpt.backend.dto.response.permission.PermissionProjectResponse;
import com.fpt.backend.service.interfaces.permission.IPermissionService;
import com.fpt.backend.util.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstant.Permission.PERMISSIONS)
@RequiredArgsConstructor
public class PermissionController {
    private final IPermissionService permissionService;

    // Lấy danh sách quyền theo bộ lọc, phân trang và thứ tự sắp xếp được yêu cầu.
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment', 'Employee')")
    @GetMapping(ApiConstant.Permission.LIST)
    public ResponseEntity<BaseResponse<PermissionListResponse>> getPermissions(@Valid @ModelAttribute PermissionListRequest request) {
        PermissionListResponse permissions = permissionService.getPermissions(request);

        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new BaseResponse<>(permissions));
    }

    // Lấy danh sách dự án mà người dùng có thể chọn khi tạo hoặc cập nhật quyền.
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment', 'Employee')")
    @GetMapping(ApiConstant.Permission.PROJECTS)
    public ResponseEntity<BaseResponse<List<PermissionProjectResponse>>> getProjectsForPermissionSelection() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new BaseResponse<>(permissionService.getProjectsForPermissionSelection()));
    }

    // Lấy toàn bộ action khả dụng để cấu hình cho một quyền.
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment', 'Employee')")
    @GetMapping(ApiConstant.Permission.ACTIONS)
    public ResponseEntity<BaseResponse<List<PermissionActionResponse>>>
    getAvailableActions() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new BaseResponse<>(permissionService.getAvailableActions()));
    }

    // Lấy thông tin chi tiết của một quyền theo mã định danh.
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment', 'Employee')")
    @GetMapping(ApiConstant.Permission.BY_ID)
    public ResponseEntity<BaseResponse<PermissionDetailResponse>> getPermissionById(@PathVariable UUID id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new BaseResponse<>(permissionService.getPermissionById(id)));
    }

    // Tạo quyền mới từ dữ liệu người dùng gửi lên.
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment', 'Employee')")
    @PostMapping
    public ResponseEntity<BaseResponse<PermissionDetailResponse>> createPermission(
            @Valid @RequestBody PermissionRequest request) {
        PermissionDetailResponse permission = permissionService.createPermission(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>(HttpStatus.CREATED.value(), "Created", permission));
    }

    // Cập nhật quyền hiện có theo mã định danh.
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment', 'Employee')")
    @PutMapping(ApiConstant.Permission.BY_ID)
    public ResponseEntity<BaseResponse<PermissionDetailResponse>> updatePermission(
            @PathVariable UUID id,
            @Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.ok(new BaseResponse<>(permissionService.updatePermission(id, request)));
    }

    // Xóa quyền theo mã định danh sau khi service kiểm tra điều kiện nghiệp vụ.
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment', 'Employee')")
    @DeleteMapping(ApiConstant.Permission.BY_ID)
    public ResponseEntity<BaseResponse<Void>> deletePermission(@PathVariable UUID id) {
        permissionService.deletePermission(id);

        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "Deleted", null));
    }
}
