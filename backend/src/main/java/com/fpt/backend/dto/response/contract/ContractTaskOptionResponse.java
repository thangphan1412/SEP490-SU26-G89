package com.fpt.backend.dto.response.contract;

import java.util.UUID;

public record ContractTaskOptionResponse(
        UUID id,
        String title,
        String status,
        UUID assignedUserId,
        String assignedUserName
) {
}
