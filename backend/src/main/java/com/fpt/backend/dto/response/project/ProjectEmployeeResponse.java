package com.fpt.backend.dto.response.project;

import com.fpt.backend.enums.UserStatus;

import java.util.UUID;

public record ProjectEmployeeResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserStatus status
) {
}
