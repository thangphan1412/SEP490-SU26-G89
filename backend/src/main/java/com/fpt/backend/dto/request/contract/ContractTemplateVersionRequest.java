package com.fpt.backend.dto.request.contract;

import java.util.List;

public record ContractTemplateVersionRequest(
        String versionName,
        String templateContent,
        String layoutJson,
        String changeNote,
        String createdBy,
        Integer pageCount,
        List<ContractPositionRequest> positions
) {
}
