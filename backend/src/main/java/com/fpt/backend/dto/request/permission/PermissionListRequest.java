package com.fpt.backend.dto.request.permission;

public record PermissionListRequest(
        String search,
        Integer projectId,
        Integer roleId,
        Boolean status,
        int page,
        String sortBy,
        String sortDirection
) {
}
