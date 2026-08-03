package com.fpt.backend.service.interfaces.department;

import com.fpt.backend.dto.request.department.DepartmentRequestDTO;
import com.fpt.backend.dto.response.department.DepartmentResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IDepartmentService {
    List<DepartmentResponseDTO> getAllDepartments();

    List<DepartmentResponseDTO> searchDepartments(String search, String status);

    DepartmentResponseDTO getDepartmentById(UUID id);

    DepartmentResponseDTO createDepartment(DepartmentRequestDTO request);

    DepartmentResponseDTO updateDepartment(UUID id, DepartmentRequestDTO request);
}
