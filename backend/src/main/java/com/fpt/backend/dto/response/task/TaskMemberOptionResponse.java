package com.fpt.backend.dto.response.task;

import java.util.UUID;

public record TaskMemberOptionResponse(
        UUID id,
        String name,
        String email
) {
}
