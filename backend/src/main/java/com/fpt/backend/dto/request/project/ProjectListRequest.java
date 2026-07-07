package com.fpt.backend.dto.request.project;

public record ProjectListRequest(
        String search,
        String status,
        int page,
        String sortBy,
        String sortDirection
) {
}
