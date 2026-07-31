package com.fpt.backend.dto.request.project;

public record ProjectListRequest(
        String search,
        String status,
        boolean viewOnlyYourProjects,
        int page,
        String sortBy,
        String sortDirection
) {
}
