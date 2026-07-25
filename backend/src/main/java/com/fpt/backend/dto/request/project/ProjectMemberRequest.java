package com.fpt.backend.dto.request.project;

import java.util.UUID;

public record ProjectMemberRequest(
        UUID userId,
        UUID permissionId
) {
}
