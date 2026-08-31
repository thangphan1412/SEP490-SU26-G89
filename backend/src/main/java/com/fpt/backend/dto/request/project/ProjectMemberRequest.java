package com.fpt.backend.dto.request.project;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProjectMemberRequest(
        @NotNull(message = "Project member user is required")
        UUID userId,
        UUID permissionId
) {
}
