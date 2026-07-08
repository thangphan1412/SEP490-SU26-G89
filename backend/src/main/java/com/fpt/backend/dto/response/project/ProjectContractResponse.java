package com.fpt.backend.dto.response.project;

public record ProjectContractResponse(
        String contractTitle,
        String contractNumber,
        String contractStatus
) {
}
