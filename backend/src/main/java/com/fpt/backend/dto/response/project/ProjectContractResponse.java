package com.fpt.backend.dto.response.project;

import java.util.UUID;

public record ProjectContractResponse(
        UUID id,
        String contractTitle,
        String contractNumber,
        String contractStatus
) {
}
