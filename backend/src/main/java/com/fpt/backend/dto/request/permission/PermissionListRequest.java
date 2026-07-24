package com.fpt.backend.dto.request.permission;

import java.util.UUID;

public record PermissionListRequest(
        String search,
        UUID projectId,
        UUID roleId,
        Boolean status,
        int page,
        String sortBy,
        String sortDirection
) {

}
