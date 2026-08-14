package com.fpt.backend.controller.projectController;

import com.fpt.backend.constant.ApiConstant;
import com.fpt.backend.dto.request.project.ProjectCreateRequest;
import com.fpt.backend.dto.request.project.ProjectListRequest;
import com.fpt.backend.dto.request.project.ProjectPermissionConfigurationRequest;
import com.fpt.backend.dto.request.project.ProjectUpdateRequest;
import com.fpt.backend.dto.response.project.ProjectDetailResponse;
import com.fpt.backend.dto.response.project.ProjectEmployeeResponse;
import com.fpt.backend.dto.response.project.ProjectListResponse;
import com.fpt.backend.dto.response.project.ProjectPermissionConfigurationResponse;
import com.fpt.backend.enums.ProjectDeleteResult;
import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.service.interfaces.project.IProjectService;
import com.fpt.backend.util.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping(ApiConstant.Project.PROJECTS)
@RequiredArgsConstructor
public class ProjectController {
    private final IProjectService projectService;

    // Lấy danh sách dự án theo điều kiện lọc và phân trang.
    @GetMapping
    public ResponseEntity<BaseResponse<ProjectListResponse>> getProjects(
            @ModelAttribute ProjectListRequest request) {
        ProjectListResponse projects = projectService.getProjects(request);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(projects));
    }

    // Lấy thông tin chi tiết của một dự án theo mã định danh.
    @GetMapping(ApiConstant.Project.BY_ID)
    public ResponseEntity<BaseResponse<ProjectDetailResponse>> getProjectById(@PathVariable UUID id) {
        ProjectDetailResponse project = projectService.getProjectById(id);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(project));
    }

    // Lấy danh sách nhân viên có thể được thêm vào dự án.
    @GetMapping(ApiConstant.Project.EMPLOYEES)
    public ResponseEntity<BaseResponse<List<ProjectEmployeeResponse>>> getEmployeesForProjectSelection() {
        List<ProjectEmployeeResponse> employees = projectService.getEmployeesForProjectSelection();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(employees));
    }

    // Lấy các trạng thái người dùng dùng cho bộ lọc thành viên dự án.
    @GetMapping(ApiConstant.Project.USER_STATUSES)
    public ResponseEntity<BaseResponse<List<UserStatus>>> getUserStatusesForProjectMemberFilter() {
        List<UserStatus> statuses = projectService.getUserStatusesForProjectMemberFilter();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(statuses));
    }

    // Lấy cấu hình quyền hiện tại của một dự án.
    @GetMapping(ApiConstant.Project.PERMISSION_CONFIGURATIONS)
    public ResponseEntity<BaseResponse<List<ProjectPermissionConfigurationResponse>>>
    getProjectPermissionConfigurations(@PathVariable UUID projectId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(
                        projectService.getProjectPermissionConfigurations(projectId)
                ));
    }

    // Cập nhật các action và phạm vi làm việc cho một quyền trong dự án.
    @PutMapping(ApiConstant.Project.PERMISSION_BY_ID)
    public ResponseEntity<BaseResponse<ProjectPermissionConfigurationResponse>>
    configureProjectPermission(
            @PathVariable UUID projectId,
            @PathVariable UUID permissionId,
            @RequestBody ProjectPermissionConfigurationRequest request) {
        return ResponseEntity.ok(new BaseResponse<>(
                projectService.configureProjectPermission(projectId, permissionId, request)
        ));
    }

    // Tạo dự án mới cùng các thông tin cấu hình liên quan.
    @PostMapping
    public ResponseEntity<BaseResponse<ProjectDetailResponse>> createProject(@RequestBody ProjectCreateRequest request) {
        ProjectDetailResponse project = projectService.createProject(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse<>(
                        HttpStatus.CREATED.value(),
                        "Created",
                        project
                ));
    }

    // Cập nhật thông tin và cấu hình của một dự án hiện có.
    @PutMapping(ApiConstant.Project.BY_ID)
    public ResponseEntity<BaseResponse<ProjectDetailResponse>> updateProject(
            @PathVariable UUID id,
            @RequestBody ProjectUpdateRequest request) {
        return ResponseEntity.ok(new BaseResponse<>(projectService.updateProject(id, request)));
    }

    // Phê duyệt dự án theo cấp duyệt của người dùng hiện tại.
    @PostMapping(ApiConstant.Project.APPROVE_BY_ID)
    public ResponseEntity<BaseResponse<Void>> approveProject(
            @PathVariable UUID id) {
        projectService.approveProject(id);

        return ResponseEntity.ok(new BaseResponse<>(
                HttpStatus.OK.value(),
                "Project approved successfully",
                null
        ));
    }

    // Xóa dự án hoặc chuyển dự án sang trạng thái hủy khi đã có hợp đồng.
    @DeleteMapping(ApiConstant.Project.BY_ID)
    public ResponseEntity<BaseResponse<Void>> deleteProject(@PathVariable UUID id) {
        ProjectDeleteResult deleteResult = projectService.deleteProject(id);
        String message;

        // Chọn thông báo phản hồi theo kết quả xóa thực tế của dự án.
        if (deleteResult == ProjectDeleteResult.DELETED_PERMANENTLY) {
            message = "Project deleted permanently";
        } else {
            message = "Project has contracts, so its status was changed to Cancelled";
        }

        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), message, null));
    }
}
