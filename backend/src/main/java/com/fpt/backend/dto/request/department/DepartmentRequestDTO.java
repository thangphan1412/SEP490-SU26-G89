package com.fpt.backend.dto.request.department;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentRequestDTO {
    private String departmentName;
    private String departmentCode;
    private String departmentStatus;
}
