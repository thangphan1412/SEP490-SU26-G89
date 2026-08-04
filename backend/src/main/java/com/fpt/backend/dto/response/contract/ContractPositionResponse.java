package com.fpt.backend.dto.response.contract;

import java.util.UUID;

public record ContractPositionResponse(
        UUID id,
        String attributeKey,
        String fieldLabel,
        Integer pageNumber,
        Double xPosition,
        Double yPosition,
        Double width,
        Double height,
        String fieldType,
        String valueSource,
        String signerRole,
        Boolean systemField,
        Boolean required
) {
}
