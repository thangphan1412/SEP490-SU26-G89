package com.fpt.backend.dto.response.department;

import com.fpt.backend.entity.Departments;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentResponseDTO {
    private Integer id;
    private String departmentName;
    private String departmentCode;
    private LocalDate departmentCreateAt;
    private String departmentStatus;

    public static DepartmentResponseDTO fromEntity(Departments department) {
        return DepartmentResponseDTO.builder()
                .id(department.getId())
                .departmentName(department.getDepartmentName())
                .departmentCode(department.getDepartmentCode())
                .departmentCreateAt(department.getDepartmentCreateAt())
                .departmentStatus(department.getDepartmentStatus())
                .build();
    }
}
