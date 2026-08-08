package com.fpt.backend.dto.response.project;

import java.util.List;

public record ProjectListResponse(
        List<ProjectListItemResponse> items,
        long totalElements,
        int totalPages
) {
}
