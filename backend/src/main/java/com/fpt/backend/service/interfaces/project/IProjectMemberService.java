package com.fpt.backend.service.interfaces.project;

import com.fpt.backend.dto.request.project.ProjectMemberRequest;
import com.fpt.backend.dto.response.project.ProjectEmployeeResponse;
import com.fpt.backend.dto.response.project.ProjectUserResponse;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.enums.UserStatus;

import java.util.List;
import java.util.UUID;

public interface IProjectMemberService {
    List<ProjectEmployeeResponse> getEmployeesForSelection();

    List<UserStatus> getUserStatusesForFilter();

    void syncMembers(
            Projects project,
            List<ProjectMemberRequest> memberRequests,
            boolean keepExistingWhenMissing
    );

    List<ProjectUserResponse> getProjectUsers(UUID projectId);

    void deleteProjectData(UUID projectId);
}
