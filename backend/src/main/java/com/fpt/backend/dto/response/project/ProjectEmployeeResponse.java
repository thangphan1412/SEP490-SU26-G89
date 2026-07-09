package com.fpt.backend.dto.response.project;

public record ProjectEmployeeResponse(
        int id,
        String email,
        String firstName,
        String lastName,
        String role,
        String status
) {
}
