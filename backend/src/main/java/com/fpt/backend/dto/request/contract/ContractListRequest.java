package com.fpt.backend.dto.request.contract;

public record ContractListRequest(
        String search,
        String status,
        int page,
        String sortBy,
        String sortDirection
) {
}
