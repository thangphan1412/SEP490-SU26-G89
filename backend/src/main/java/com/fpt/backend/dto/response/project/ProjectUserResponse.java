package com.fpt.backend.dto.response.project;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectUserResponse(
        UUID userId,
        String email,
        String userName,
        String role,
        String userStatus,
        LocalDate joinDate,
        UUID permissionId,
        String permissionName,
        String permissionCode
) {
}
