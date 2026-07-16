package com.fpt.backend.dto.response.project;

import java.time.LocalDate;

public record ProjectUserResponse(
        int userId,
        String email,
        String userName,
        String role,
        String userStatus,
        LocalDate joinDate,
        String permissionValue,
        Integer permissionId,
        String permissionName,
        String permissionCode
) {
}
