package com.fpt.backend.dto.request.permission;

public record PermissionListRequest(
        String search,
        Integer projectId,
        int page,
        String sortBy,
        String sortDirection
) {
}
