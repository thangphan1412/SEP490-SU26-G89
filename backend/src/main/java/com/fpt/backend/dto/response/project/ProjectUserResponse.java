package com.fpt.backend.dto.response.project;

import com.fpt.backend.enums.UserStatus;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectUserResponse(
        UUID userId,
        String email,
        String userName,
        UserStatus userStatus,
        LocalDate joinDate,
        UUID permissionId,
        String permissionName,
        String permissionCode
) {
}
