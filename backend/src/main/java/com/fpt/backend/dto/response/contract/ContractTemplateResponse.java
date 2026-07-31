package com.fpt.backend.dto.response.contract;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ContractTemplateResponse(
        UUID id,
        UUID contractTypeId,
        String contractTypeCode,
        String contractTypeName,
        String contractTemplateName,
        String contractTemplateDescription,
        String status,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer latestVersion,
        long contractCount,
        List<ContractTemplateVersionResponse> versions
) {
}
