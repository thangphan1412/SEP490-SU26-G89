package com.fpt.backend.dto.request.contract;

public record ContractTemplateBlockRequest(
        String key,
        String type,
        Boolean enabled,
        String heading,
        String content,
        String leftLabel,
        String rightLabel
) {
}
