package com.fpt.backend.dto.response.task;

import java.util.UUID;

public record TaskContractResponse(
        UUID id,
        String contractNumber,
        String contractTitle
) {
}
