package com.fpt.backend.dto.response.contract;

import java.util.List;
import java.util.UUID;

public record ContractPhaseOptionResponse(
        UUID id,
        String title,
        String status,
        List<ContractTaskOptionResponse> tasks
) {
}
