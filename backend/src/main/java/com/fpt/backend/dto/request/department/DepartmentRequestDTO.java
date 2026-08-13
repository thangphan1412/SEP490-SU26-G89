package com.fpt.backend.dto.request.department;

import com.fpt.backend.enums.DepartmentStatus;
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
    private DepartmentStatus departmentStatus;
}
