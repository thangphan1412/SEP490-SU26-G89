package com.fpt.backend.dto.request.contract;

import java.util.List;

public record ContractTypeRequest(
        String contractTypeCode,
        String contractTypeName,
        String description,
        Integer validityDays,
        String category,
        String status,
        String createdBy,
        String workflowName,
        List<ContractWorkflowStepRequest> workflowSteps
) {
}
