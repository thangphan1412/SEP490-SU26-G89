package com.fpt.backend.service.interfaces;

import com.fpt.backend.dto.request.project.ProjectListRequest;
import com.fpt.backend.dto.response.project.ProjectListResponse;

public interface ProjectService {
    ProjectListResponse getProjects(ProjectListRequest request);
}
