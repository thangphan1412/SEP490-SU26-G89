package com.fpt.backend.dto.request.contract;

import java.util.UUID;

public record ContractTemplateRequest(
        UUID contractTypeId,
        String contractTemplateName,
        String contractTemplateDescription,
        String status,
        String createdBy
) {
}
