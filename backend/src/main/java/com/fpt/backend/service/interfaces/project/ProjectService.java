package com.fpt.backend.service.interfaces.project;

import com.fpt.backend.dto.request.project.ProjectCreateRequest;
import com.fpt.backend.dto.request.project.ProjectListRequest;
import com.fpt.backend.dto.request.project.ProjectUpdateRequest;
import com.fpt.backend.dto.response.project.ProjectDetailResponse;
import com.fpt.backend.dto.response.project.ProjectEmployeeResponse;
import com.fpt.backend.dto.response.project.ProjectListResponse;
import com.fpt.backend.dto.response.project.ProjectRoleResponse;

import java.util.List;

public interface ProjectService {
    ProjectListResponse getProjects(ProjectListRequest request);

    ProjectDetailResponse getProjectById(int id);

    ProjectDetailResponse createProject(ProjectCreateRequest request);

    ProjectDetailResponse updateProject(int id, ProjectUpdateRequest request);

    boolean deleteProject(int id);

    List<ProjectEmployeeResponse> getEmployeesForProjectSelection();

    List<ProjectRoleResponse> getRolesForProjectMemberFilter();
}
