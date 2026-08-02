package com.fpt.backend.dto.response.contract;

public record ContractPdfResponse(
        String fileName,
        byte[] content
) {
}
