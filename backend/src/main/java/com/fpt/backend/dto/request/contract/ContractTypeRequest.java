package com.fpt.backend.dto.request.contract;

public record ContractTypeRequest(
        String contractTypeCode,
        String contractTypeName,
        String description,
        Integer validityDays,
        String category,
        String status,
        String createdBy
) {
}
