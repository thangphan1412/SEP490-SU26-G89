package com.fpt.backend.dto.response.project;

import java.util.List;
import java.util.UUID;

public record ProjectEmployeeResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        List<ProjectRoleResponse> roles,
        String status
) {
}
