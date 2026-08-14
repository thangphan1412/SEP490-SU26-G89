package com.fpt.backend.dto.request.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProjectPermissionConfigurationRequest(
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
