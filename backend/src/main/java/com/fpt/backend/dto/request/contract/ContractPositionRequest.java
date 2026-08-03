package com.fpt.backend.dto.request.contract;

public record ContractPositionRequest(
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
