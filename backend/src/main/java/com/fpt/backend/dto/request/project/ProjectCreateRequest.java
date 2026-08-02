package com.fpt.backend.dto.request.project;

import java.time.LocalDate;
import java.util.List;

public record ProjectCreateRequest(
        String projectName,
        String projectCode,
        LocalDate projectStartDate,
        LocalDate projectEndDate,
        String projectDescription,
        String projectStatus,
        List<ProjectPhaseRequest> phases,
        List<ProjectMemberRequest> members
) {
}
