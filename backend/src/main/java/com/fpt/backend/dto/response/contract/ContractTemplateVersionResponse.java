package com.fpt.backend.dto.response.contract;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ContractTemplateVersionResponse(
        UUID id,
        Integer versionNumber,
        String versionName,
        String templateContent,
        String layoutJson,
        String changeNote,
        String createdBy,
        LocalDateTime createdAt,
        Integer pageCount,
        List<ContractPositionResponse> positions
) {
}
