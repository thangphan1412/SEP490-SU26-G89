package com.fpt.backend.dto.request.project;

public record ProjectMemberRequest(
        Integer userId,
        Integer permissionId
) {
}
