package com.fpt.backend.dto.request.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentRequestDTO {
    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Department name cannot exceed 100 characters")
    private String departmentName;

    @NotBlank(message = "Department code is required")
    @Size(
            min = 2,
            max = 50,
            message = "Department code must contain between 2 and 50 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9_]{1,49}$",
            message = "Department code must start with a letter and contain only letters, numbers, or underscores"
    )
    private String departmentCode;

    @NotBlank(message = "Department status is required")
    @Pattern(
            regexp = "(?i)^(Active|Inactive)$",
            message = "Department status must be Active or Inactive"
    )
    private String departmentStatus;
}
