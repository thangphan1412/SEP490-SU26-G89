package com.fpt.backend.service.interfaces.project;

import com.fpt.backend.dto.request.project.ProjectPermissionConfigurationRequest;
import com.fpt.backend.dto.response.project.ProjectPermissionConfigurationResponse;
import com.fpt.backend.dto.response.project.ProjectPermissionOptionResponse;
import com.fpt.backend.entity.Projects;

import java.util.List;
import java.util.UUID;

public interface ProjectPermissionService {
    UUID createProjectFullAccessPermission(Projects project);

    List<ProjectPermissionConfigurationResponse> getConfigurations(
            UUID projectId
    );

    ProjectPermissionConfigurationResponse configure(
            Projects project,
            UUID permissionId,
            ProjectPermissionConfigurationRequest request
    );

    List<ProjectPermissionOptionResponse> getOptions(UUID projectId);

    void deleteProjectData(UUID projectId);
}
