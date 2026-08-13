package com.fpt.backend.dto.request.project;

import java.time.LocalDate;
import java.util.List;

public record ProjectUpdateRequest(
        String projectName,
        String projectCode,
        LocalDate projectStartDate,
        LocalDate projectEndDate,
        String projectDescription,
        List<ProjectPhaseRequest> phases,
        List<ProjectMemberRequest> members
) {
}
