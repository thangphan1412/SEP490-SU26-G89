package com.fpt.backend.service.impl.department;

import com.fpt.backend.dto.request.department.DepartmentRequestDTO;
import com.fpt.backend.dto.response.department.DepartmentResponseDTO;
import com.fpt.backend.entity.Departments;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.department.DepartmentRepository;
import com.fpt.backend.service.interfaces.department.IDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements IDepartmentService {

    private static final int MAX_DEPARTMENT_NAME_LENGTH = 100;
    private static final Pattern DEPARTMENT_CODE_PATTERN =
            Pattern.compile("^[A-Z][A-Z0-9_]{1,49}$");

    private final DepartmentRepository departmentRepository;

    @Override
    public List<DepartmentResponseDTO> getAllDepartments() {
        return searchDepartments("", "");
    }

    @Override
    public List<DepartmentResponseDTO> searchDepartments(String search, String status) {
        return departmentRepository.searchAndFilter(
                        normalize(search).toLowerCase(Locale.ROOT),
                        normalizeStatusFilter(status)
                )
                .stream()
                .map(DepartmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentResponseDTO getDepartmentById(UUID id) {
        return DepartmentResponseDTO.fromEntity(findDepartment(id));
    }

    @Override
    @Transactional
    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO request) {
        validateRequest(request);

        String departmentCode = normalizeCode(request.getDepartmentCode());
        if (departmentRepository.existsByDepartmentCodeIgnoreCase(departmentCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Department code is already in use"
            );
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
    @Transactional
    public DepartmentResponseDTO updateDepartment(UUID id, DepartmentRequestDTO request) {
        validateRequest(request);

        Departments department = findDepartment(id);

        String departmentCode = normalizeCode(request.getDepartmentCode());
        if (departmentRepository.existsByDepartmentCodeIgnoreCaseAndIdNot(departmentCode, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Department code is already in use by another department"
            );
        }

        department.setDepartmentName(request.getDepartmentName().trim());
        department.setDepartmentCode(departmentCode);
        department.setDepartmentStatus(resolveStatus(request.getDepartmentStatus()));
        department.setUpdatedAt(LocalDateTime.now());

        Departments updatedDepartment = departmentRepository.save(department);
        return DepartmentResponseDTO.fromEntity(updatedDepartment);
    }

    private Departments findDepartment(UUID id) {
        if (id == null) {
            throw new BadHttpException("Department id is required");
        }

        return departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found"));
    }

    private void validateRequest(DepartmentRequestDTO request) {
        if (request == null) {
            throw new BadHttpException("Department information is required");
        }

        if (request.getDepartmentName() == null || request.getDepartmentName().isBlank()) {
            throw new BadHttpException("Department name is required");
        }

        if (request.getDepartmentName().trim().length() > MAX_DEPARTMENT_NAME_LENGTH) {
            throw new BadHttpException("Department name cannot exceed 100 characters");
        }

        String departmentCode = normalizeCode(request.getDepartmentCode());
        if (!DEPARTMENT_CODE_PATTERN.matcher(departmentCode).matches()) {
            throw new BadHttpException(
                    "Department code must start with a letter and contain 2-50 uppercase letters, numbers, or underscores"
            );
        }

        resolveStatus(request.getDepartmentStatus());
    }

    private String normalizeCode(String departmentCode) {
        return normalize(departmentCode).toUpperCase(Locale.ROOT);
    }

    private String resolveStatus(String departmentStatus) {
        String normalizedStatus = normalize(departmentStatus);

        if ("active".equalsIgnoreCase(normalizedStatus)) {
            return "Active";
        }

        if ("inactive".equalsIgnoreCase(normalizedStatus)) {
            return "Inactive";
        }

        throw new BadHttpException("Department status must be Active or Inactive");
    }

    private String normalizeStatusFilter(String status) {
        String normalizedStatus = normalize(status);

        if (normalizedStatus.isEmpty()) {
            return "";
        }

        if ("active".equalsIgnoreCase(normalizedStatus)
                || "inactive".equalsIgnoreCase(normalizedStatus)) {
            return normalizedStatus.toLowerCase(Locale.ROOT);
        }

        throw new BadHttpException("Department status filter must be Active or Inactive");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
