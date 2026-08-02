package com.fpt.backend.dto.request.contract;

public record ContractTemplateVersionRequest(
        String versionName,
        String templateContent,
        String layoutJson,
        String changeNote,
        String createdBy
) {
}
