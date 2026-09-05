package com.fpt.backend.dto.response.contract;

import java.util.List;
import java.util.UUID;

public record ContractProjectMemberOptionResponse(
        UUID userId,
        String fullName,
        String email,
        String roleCode,
        List<String> roleCodes,
        UUID departmentId,
        String departmentCode,
        String departmentName,
        List<String> allowedActions
) {
}
