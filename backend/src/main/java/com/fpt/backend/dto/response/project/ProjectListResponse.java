package com.fpt.backend.dto.response.project;

import java.util.List;

public record ProjectListResponse(
        String source,
        List<ProjectListItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        List<String> availableStatuses
) {
}
