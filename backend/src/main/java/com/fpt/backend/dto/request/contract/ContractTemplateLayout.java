package com.fpt.backend.dto.request.contract;

import java.util.List;

public record ContractTemplateLayout(
        Integer pageCount,
        String coordinateSystem,
        List<ContractPositionRequest> fields
) {
}
