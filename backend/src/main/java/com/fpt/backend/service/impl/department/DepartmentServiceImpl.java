package com.fpt.backend.service.impl.department;

import com.fpt.backend.dto.request.department.DepartmentRequestDTO;
import com.fpt.backend.dto.response.department.DepartmentResponseDTO;
import com.fpt.backend.entity.Departments;
import com.fpt.backend.repository.department.DepartmentRepository;
import com.fpt.backend.service.interfaces.department.IDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements IDepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public List<DepartmentResponseDTO> getAllDepartments() {
        return searchDepartments("", "");
    }

    @Override
    public List<DepartmentResponseDTO> searchDepartments(String search, String status) {
        return departmentRepository.searchAndFilter(
                        normalize(search).toLowerCase(Locale.ROOT),
                        normalize(status).toLowerCase(Locale.ROOT)
                )
                .stream()
                .map(DepartmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentResponseDTO getDepartmentById(Integer id) {
        Departments department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        return DepartmentResponseDTO.fromEntity(department);
    }

    @Override
    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO request) {
        validateRequest(request);

        String departmentCode = normalizeCode(request.getDepartmentCode());
        if (departmentRepository.existsByDepartmentCodeIgnoreCase(departmentCode)) {
            throw new RuntimeException("Department code is already in use!");
        }

        Departments department = Departments.builder()
                .departmentName(request.getDepartmentName().trim())
                .departmentCode(departmentCode)
                .departmentStatus(resolveStatus(request.getDepartmentStatus()))
                .departmentCreatedAt(LocalDateTime.now())
                .updatedAt(null)
                .company(null)
                .build();

        Departments savedDepartment = departmentRepository.save(department);
        return DepartmentResponseDTO.fromEntity(savedDepartment);
    }

    @Override
    public DepartmentResponseDTO updateDepartment(Integer id, DepartmentRequestDTO request) {
        validateRequest(request);

        Departments department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        String departmentCode = normalizeCode(request.getDepartmentCode());
        if (departmentRepository.existsByDepartmentCodeIgnoreCaseAndIdNot(departmentCode, id)) {
            throw new RuntimeException("Department code is already in use by another department!");
        }

        department.setDepartmentName(request.getDepartmentName().trim());
        department.setDepartmentCode(departmentCode);
        department.setDepartmentStatus(resolveStatus(request.getDepartmentStatus()));
        department.setUpdatedAt(LocalDateTime.now());

        Departments updatedDepartment = departmentRepository.save(department);
        return DepartmentResponseDTO.fromEntity(updatedDepartment);
    }

    private void validateRequest(DepartmentRequestDTO request) {
        if (request == null) {
            throw new RuntimeException("Department request is required");
        }

        if (request.getDepartmentName() == null || request.getDepartmentName().isBlank()) {
            throw new RuntimeException("Department name is required");
        }

        if (request.getDepartmentCode() == null || request.getDepartmentCode().isBlank()) {
            throw new RuntimeException("Department code is required");
        }
    }

    private String normalizeCode(String departmentCode) {
        return departmentCode.trim().toUpperCase(Locale.ROOT);
    }

    private String resolveStatus(String departmentStatus) {
        return departmentStatus == null || departmentStatus.isBlank()
                ? "Active"
                : departmentStatus;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
