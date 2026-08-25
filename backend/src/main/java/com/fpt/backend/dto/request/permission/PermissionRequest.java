package com.fpt.backend.dto.request.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record PermissionRequest(
        @NotBlank(message = "Permission name is required")
        @Size(max = 50, message = "Permission name must not exceed 50 characters")
        String permissionName,

        @NotBlank(message = "Permission code is required")
        @Size(max = 50, message = "Permission code must not exceed 50 characters")
        String permissionCode,

        @Size(max = 255, message = "Permission description must not exceed 255 characters")
        String permissionDescription,

        @NotNull(message = "Permission status is required")
        Boolean status,

        @NotNull(message = "Project is required")
        UUID projectId,

        @NotNull(message = "Allowed actions are required")
        List<
                @NotBlank(message = "Permission action code must not be blank")
                @Size(max = 50, message = "Permission action code must not exceed 50 characters")
                String> allowedActions,

        @NotBlank(message = "Work scope is required")
        @Pattern(regexp = "(?i)^(OWN|FULL)$", message = "Work scope must be OWN or FULL")
        String workScope
) {
}
