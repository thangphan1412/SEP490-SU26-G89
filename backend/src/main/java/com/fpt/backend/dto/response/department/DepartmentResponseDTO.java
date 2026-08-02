package com.fpt.backend.dto.response.department;

import com.fpt.backend.entity.Departments;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentResponseDTO {
    private Integer id;
    private Integer companyId;
    private String departmentName;
    private String departmentCode;
    private String departmentStatus;
    private LocalDateTime departmentCreatedAt;
    private LocalDateTime updatedAt;

    public static DepartmentResponseDTO fromEntity(Departments department) {
        return DepartmentResponseDTO.builder()
                .id(department.getId())
                .companyId(department.getCompany() == null ? null : department.getCompany().getId())
                .departmentName(department.getDepartmentName())
                .departmentCode(department.getDepartmentCode())
                .departmentStatus(department.getDepartmentStatus())
                .departmentCreatedAt(department.getDepartmentCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}
