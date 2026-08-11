package com.fpt.backend.dto.response.contract;

import java.util.UUID;

public record ContractRoleOptionResponse(
        UUID id,
        String roleCode,
        String roleName
) {
}
