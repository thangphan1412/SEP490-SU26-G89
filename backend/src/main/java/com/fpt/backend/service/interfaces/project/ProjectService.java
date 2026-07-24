package com.fpt.backend.service.interfaces.project;

import com.fpt.backend.dto.request.project.ProjectCreateRequest;
import com.fpt.backend.dto.request.project.ProjectListRequest;
import com.fpt.backend.dto.request.project.ProjectPermissionConfigurationRequest;
import com.fpt.backend.dto.request.project.ProjectUpdateRequest;
import com.fpt.backend.dto.response.project.ProjectDetailResponse;
import com.fpt.backend.dto.response.project.ProjectEmployeeResponse;
import com.fpt.backend.dto.response.project.ProjectListResponse;
import com.fpt.backend.dto.response.project.ProjectPermissionConfigurationResponse;
import com.fpt.backend.dto.response.project.ProjectRoleResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    ProjectListResponse getProjects(ProjectListRequest request);

    ProjectDetailResponse getProjectById(UUID id);

    ProjectDetailResponse createProject(ProjectCreateRequest request);

    ProjectDetailResponse updateProject(UUID id, ProjectUpdateRequest request);

    boolean deleteProject(UUID id);

    List<ProjectEmployeeResponse> getEmployeesForProjectSelection();

    List<ProjectRoleResponse> getRolesForProjectMemberFilter();

    List<ProjectPermissionConfigurationResponse> getProjectPermissionConfigurations(UUID projectId);

    ProjectPermissionConfigurationResponse configureProjectPermission(
            UUID projectId,
            UUID permissionId,
            ProjectPermissionConfigurationRequest request
    );
}
