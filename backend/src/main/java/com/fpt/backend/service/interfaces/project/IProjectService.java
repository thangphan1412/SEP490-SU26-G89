package com.fpt.backend.service.interfaces.project;

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

import java.util.List;
import java.util.UUID;

public interface IProjectService {
    // Tìm kiếm và phân trang danh sách dự án theo request.
    ProjectListResponse getProjects(ProjectListRequest request);

    // Lấy chi tiết dự án theo mã định danh.
    ProjectDetailResponse getProjectById(UUID id);

    // Tạo dự án mới từ request.
    ProjectDetailResponse createProject(ProjectCreateRequest request);

    // Cập nhật dự án hiện có từ request.
    ProjectDetailResponse updateProject(UUID id, ProjectUpdateRequest request);

    // Phê duyệt dự án theo người dùng hiện tại.
    void approveProject(UUID id);

    // Xóa dự án hoặc đổi trạng thái khi dự án đã có dữ liệu phụ thuộc.
    ProjectDeleteResult deleteProject(UUID id);

    // Lấy danh sách nhân viên có thể chọn cho dự án.
    List<ProjectEmployeeResponse> getEmployeesForProjectSelection();

    // Lấy danh sách trạng thái dùng để lọc thành viên dự án.
    List<UserStatus> getUserStatusesForProjectMemberFilter();

    // Lấy toàn bộ cấu hình quyền thuộc một dự án.
    List<ProjectPermissionConfigurationResponse> getProjectPermissionConfigurations(UUID projectId);

    // Cập nhật một cấu hình quyền thuộc dự án.
    ProjectPermissionConfigurationResponse configureProjectPermission(
            UUID projectId,
            UUID permissionId,
            ProjectPermissionConfigurationRequest request
    );
}
