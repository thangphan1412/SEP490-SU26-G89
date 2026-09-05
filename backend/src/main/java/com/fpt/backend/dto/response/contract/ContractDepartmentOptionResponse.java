package com.fpt.backend.dto.response.contract;

import java.util.UUID;

public record ContractDepartmentOptionResponse(
        UUID id,
        String departmentCode,
        String departmentName
) {
}
