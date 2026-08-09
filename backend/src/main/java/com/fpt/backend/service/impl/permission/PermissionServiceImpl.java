package com.fpt.backend.service.impl.permission;

import com.fpt.backend.dto.request.permission.PermissionListRequest;
import com.fpt.backend.dto.request.permission.PermissionRequest;
import com.fpt.backend.dto.response.permission.PermissionActionResponse;
import com.fpt.backend.dto.response.permission.PermissionDetailResponse;
import com.fpt.backend.dto.response.permission.PermissionListItemResponse;
import com.fpt.backend.dto.response.permission.PermissionListResponse;
import com.fpt.backend.dto.response.permission.PermissionProjectResponse;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.enums.PermissionModule;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.permission.PermissionRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.permission.IPermissionAccessService;
import com.fpt.backend.service.interfaces.permission.IPermissionService;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements IPermissionService {
    private static final int PAGE_SIZE = 7;
    private static final String DEFAULT_SORT_FIELD = "createdAt";
    private static final String MANAGE_MEMBERS = PermissionModule.MANAGE_MEMBERS.name();
    private static final Set<String> SORT_FIELDS = Set.of(
            "id",
            "permissionName",
            "permissionCode",
            "permissionDescription",
            "status",
            "projectName",
            "createdAt");

    private final PermissionRepository permissionRepository;
    private final ProjectRepository projectRepository;
    private final PermissionActionServiceImpl permissionActionService;
    private final IPermissionAccessService permissionAccessService;
    private final CurrentUser currentUser;

    @Override
    public PermissionListResponse getPermissions(PermissionListRequest request) {
        String search = normalize(request.search());
        Pageable pageable = createPageable(
                request.page(),
                request.sortBy(),
                request.sortDirection());
        Page<Permissions> permissions = permissionRepository.searchPermissions(
                search.toLowerCase(Locale.ROOT),
                request.projectId(),
                request.status(),
                currentUser.getCurrentUser().getId(),
                MANAGE_MEMBERS,
                pageable);

        return new PermissionListResponse(
                permissions.map(this::toListItem).getContent(),
                permissions.getNumber(),
                permissions.getSize(),
                permissions.getTotalElements(),
                permissions.getTotalPages(),
                permissions.isFirst(),
                permissions.isLast());
    }

    @Override
    public PermissionDetailResponse getPermissionById(UUID id) {
        Permissions permission = findPermission(id);
        requireManagePermission(permission);
        return toDetail(permission);
    }

    @Override
    @Transactional
    public PermissionDetailResponse createPermission(PermissionRequest request) {
        Permissions permission = new Permissions();
        applyRequest(permission, request, null);
        return toDetail(permissionRepository.save(permission));
    }

    @Override
    @Transactional
    public PermissionDetailResponse updatePermission(UUID id, PermissionRequest request) {
        Permissions permission = findPermission(id);
        requireManagePermission(permission);
        applyRequest(permission, request, id);
        return toDetail(permissionRepository.save(permission));
    }

    @Override
    @Transactional
    public void deletePermission(UUID id) {
        Permissions permission = findPermission(id);
        requireManagePermission(permission);
        permissionRepository.delete(permission);
    }

    // Trả về danh sách các dự án mà người dùng hiện tại có quyền quản lý thành viên
    @Override
    public List<PermissionProjectResponse> getProjectsForPermissionSelection() {
        List<UUID> projectIds = permissionAccessService
                .getCurrentUserProjectIdsWithAction(MANAGE_MEMBERS);
        List<Projects> projects = projectRepository.findAllById(projectIds);
        //Sắp xếp danh sách dự án theo tên dự án (không phân biệt chữ hoa chữ thường)
        projects.sort(
                Comparator.comparing(
                        Projects::getProjectName,
                        String.CASE_INSENSITIVE_ORDER));
                        
        List<PermissionProjectResponse> responses = new ArrayList<>();

        for (Projects project : projects) {
            responses.add(new PermissionProjectResponse(
                    project.getId(),
                    project.getProjectCode(),
                    project.getProjectName()));
        }

        return responses;
    }

    @Override
    public List<PermissionActionResponse> getAvailableActions() {
        return permissionActionService.getAvailableActions();
    }

    private void applyRequest(Permissions permission, PermissionRequest request, UUID currentId) {
        if (request == null) {
            throw new BadHttpException("Permission information is required");
        }

        String permissionName = requireText(request.permissionName(), "Permission name is required", 50);
        String permissionCode = requireText(request.permissionCode(), "Permission code is required", 50);
        String permissionDescription = normalize(request.permissionDescription());
        validateMaxLength(permissionDescription, "Permission description", 255);
        Projects project = findProject(request.projectId());
        permissionAccessService.requireAction(
                project.getId(),
                MANAGE_MEMBERS);

        boolean duplicateCode = currentId == null
                ? permissionRepository.existsByPermissionCodeIgnoreCase(permissionCode)
                : permissionRepository.existsByPermissionCodeIgnoreCaseAndIdNot(permissionCode, currentId);

        if (duplicateCode) {
            throw new BadHttpException("Permission code already exists");
        }

        permission.setPermissionName(permissionName);
        permission.setPermissionCode(permissionCode);
        permission.setPermissionDescription(permissionDescription);
        permission.setStatus(request.status() == null || request.status());
        permission.setProject(project);

        permissionActionService.configurePermission(
                permission,
                request.allowedActions(),
                request.workScope());

        if (permission.getCreatedAt() == null) {
            permission.setCreatedAt(LocalDateTime.now());
        }
    }

    private Permissions findPermission(UUID id) {
        Optional<Permissions> permission = permissionRepository.findById(id);

        if (permission.isEmpty()) {
            throw new NotFoundException("Permission not found");
        }

        return permission.get();
    }

    private Projects findProject(UUID projectId) {
        if (projectId == null) {
            throw new BadHttpException("Project is required");
        }

        Optional<Projects> project = projectRepository.findById(projectId);

        if (project.isEmpty()) {
            throw new NotFoundException("Project not found");
        }

        return project.get();
    }

    private void requireManagePermission(Permissions permission) {
        Projects project = permission.getProject();

        if (project == null) {
            throw new BadHttpException(
                    "Permission is not connected to a project");
        }

        permissionAccessService.requireAction(
                project.getId(),
                MANAGE_MEMBERS);
    }

    private Pageable createPageable(int page, String sortBy, String sortDirection) {
        int validPage = Math.max(page, 0);
        String sortField = sortBy != null && SORT_FIELDS.contains(sortBy) ? sortBy : DEFAULT_SORT_FIELD;

        if ("projectName".equals(sortField)) {
            sortField = "project.projectName";
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(validPage, PAGE_SIZE, Sort.by(direction, sortField));
    }

    private String requireText(String value, String missingMessage, int maxLength) {
        String normalizedValue = normalize(value);

        if (normalizedValue.isBlank()) {
            throw new BadHttpException(missingMessage);
        }

        if (normalizedValue.length() > maxLength) {
            throw new BadHttpException(
                    "Value must not be longer than " + maxLength + " characters");
        }

        return normalizedValue;
    }

    private void validateMaxLength(String value, String fieldName, int maxLength) {
        if (value.length() > maxLength) {
            throw new BadHttpException(
                    fieldName + " must not be longer than " + maxLength + " characters");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private PermissionListItemResponse toListItem(Permissions permission) {
        Projects project = permission.getProject();

        return new PermissionListItemResponse(
                permission.getId(),
                permission.getPermissionName(),
                permission.getPermissionCode(),
                permission.getPermissionDescription(),
                permission.getStatus(),
                project == null ? null : project.getId(),
                project == null ? null : project.getProjectCode(),
                project == null ? null : project.getProjectName(),
                permission.getCreatedAt());
    }

    private PermissionDetailResponse toDetail(Permissions permission) {
        Projects project = permission.getProject();

        return new PermissionDetailResponse(
                permission.getId(),
                permission.getPermissionName(),
                permission.getPermissionCode(),
                permissionActionService.getAllowedActionCodes(permission),
                permissionActionService.getActionDetails(permission),
                permissionActionService.getWorkScope(permission),
                permission.getPermissionDescription(),
                permission.getStatus(),
                project == null ? null : project.getId(),
                project == null ? null : project.getProjectCode(),
                project == null ? null : project.getProjectName(),
                permission.getCreatedAt());
    }
}
