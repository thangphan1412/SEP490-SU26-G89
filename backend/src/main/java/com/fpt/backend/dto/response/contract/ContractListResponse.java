package com.fpt.backend.dto.response.contract;

import java.util.List;

public record ContractListResponse(
        String source,
        List<ContractResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        List<String> availableStatuses
) {
}
