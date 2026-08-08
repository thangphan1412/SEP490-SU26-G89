package com.fpt.backend.service.interfaces.project;

import com.fpt.backend.dto.request.project.ProjectPhaseRequest;
import com.fpt.backend.dto.response.project.ProjectPhaseResponse;
import com.fpt.backend.entity.Projects;

import java.util.List;
import java.util.UUID;

public interface IProjectPhaseService {
    void syncPhases(
            Projects project,
            List<ProjectPhaseRequest> phaseRequests
    );

    List<ProjectPhaseResponse> getProjectPhases(UUID projectId);

    void deleteProjectData(UUID projectId);
}
