package com.fpt.backend.dto.response.contract;

import java.util.List;
import java.util.UUID;

public record ContractProjectMemberOptionResponse(
        UUID userId,
        String fullName,
        String email,
        String roleCode,
        List<String> roleCodes,
        List<String> allowedActions
) {
}
