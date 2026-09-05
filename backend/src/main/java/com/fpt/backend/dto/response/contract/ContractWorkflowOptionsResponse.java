package com.fpt.backend.dto.response.contract;

import java.util.List;

public record ContractWorkflowOptionsResponse(
        List<String> actionTypes,
        List<ContractRoleOptionResponse> roles,
        List<ContractDepartmentOptionResponse> departments
) {
}
