package com.fpt.backend.dto.request.permission;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PermissionListRequest(
        @Size(max = 255, message = "Search text must not exceed 255 characters")
        String search,

        UUID projectId,

        Boolean status,

        @PositiveOrZero(message = "Page index must be zero or greater")
        int page,

        @Pattern(
                regexp = "^(id|permissionName|permissionCode|permissionDescription|status|projectName|createdAt)$",
                message = "Unsupported permission sort field"
        )
        String sortBy,

        @Pattern(regexp = "(?i)^(asc|desc)$", message = "Sort direction must be asc or desc")
        String sortDirection
) {
}
