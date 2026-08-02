package com.fpt.backend.dto.response.contract;

import java.util.UUID;

public record ContractProjectOptionResponse(
        UUID id,
        String projectCode,
        String projectName
) {
}
