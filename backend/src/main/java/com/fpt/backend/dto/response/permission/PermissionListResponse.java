package com.fpt.backend.dto.response.permission;

import java.util.List;

public record PermissionListResponse(
        String source,
        List<PermissionListItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
