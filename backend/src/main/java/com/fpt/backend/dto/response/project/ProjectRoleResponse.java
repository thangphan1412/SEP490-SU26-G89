package com.fpt.backend.dto.response.project;

import java.util.UUID;

public record ProjectRoleResponse(
        UUID id,
        String roleName
) {
}
