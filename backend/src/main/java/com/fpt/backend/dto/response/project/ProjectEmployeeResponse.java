package com.fpt.backend.dto.response.project;

import java.util.List;

public record ProjectEmployeeResponse(
        int id,
        String email,
        String firstName,
        String lastName,
        List<ProjectRoleResponse> roles,
        String status
) {
}
