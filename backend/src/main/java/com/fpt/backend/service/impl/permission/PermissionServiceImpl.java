package com.fpt.backend.service.impl.permission;

import com.fpt.backend.dto.request.permission.PermissionListRequest;
import com.fpt.backend.dto.request.permission.PermissionRequest;
import com.fpt.backend.dto.response.permission.PermissionDetailResponse;
import com.fpt.backend.dto.response.permission.PermissionListItemResponse;
import com.fpt.backend.dto.response.permission.PermissionListResponse;
import com.fpt.backend.dto.response.permission.PermissionProjectResponse;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.repository.permission.PermissionRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.permission.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {
    private static final int PAGE_SIZE = 7;
    private static final String DATA_SOURCE = "DATABASE";
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final Set<String> SORT_FIELDS = Set.of(
            "id",
            "permissionName",
            "permissionCode",
            "permissionModule",
            "projectName"
    );

    private final PermissionRepository permissionRepository;
    private final ProjectRepository projectRepository;

    @Override
    public PermissionListResponse getPermissions(PermissionListRequest request) {
        PermissionListRequest validRequest = request == null
                ? new PermissionListRequest("", null, 0, DEFAULT_SORT_FIELD, "desc")
                : request;
        String search = normalize(validRequest.search());
        Pageable pageable = createPageable(
                validRequest.page(),
                validRequest.sortBy(),
                validRequest.sortDirection()
        );
        Page<Permissions> permissions = findPermissions(search, validRequest.projectId(), pageable);

        return new PermissionListResponse(
                DATA_SOURCE,
                permissions.map(this::toListItem).getContent(),
                permissions.getNumber(),
                permissions.getSize(),
                permissions.getTotalElements(),
                permissions.getTotalPages(),
                permissions.isFirst(),
                permissions.isLast()
        );
    }

    @Override
    public PermissionDetailResponse getPermissionById(int id) {
        return toDetail(findPermission(id));
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
    public PermissionDetailResponse updatePermission(int id, PermissionRequest request) {
        Permissions permission = findPermission(id);
        applyRequest(permission, request, id);
        return toDetail(permissionRepository.save(permission));
    }

    @Override
    @Transactional
    public void deletePermission(int id) {
        permissionRepository.delete(findPermission(id));
    }

    @Override
    public List<PermissionProjectResponse> getProjectsForPermissionSelection() {
        return projectRepository.findAll(Sort.by(Sort.Direction.ASC, "projectName"))
                .stream()
                .map(project -> new PermissionProjectResponse(
                        project.getId(),
                        project.getProjectCode(),
                        project.getProjectName()
                ))
                .toList();
    }

    private Page<Permissions> findPermissions(String search, Integer projectId, Pageable pageable) {
        if (search.isBlank() && projectId == null) {
            return permissionRepository.findAll(pageable);
        }

        if (search.isBlank()) {
            return permissionRepository.findByProject_Id(projectId, pageable);
        }

        return permissionRepository.searchPermissions(
                search.toLowerCase(Locale.ROOT),
                projectId,
                pageable
        );
    }

    private void applyRequest(Permissions permission, PermissionRequest request, Integer currentId) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Permission information is required");
        }

        String permissionName = requireText(request.permissionName(), "Permission name is required", 50);
        String permissionCode = requireText(request.permissionCode(), "Permission code is required", 50);
        String permissionModule = requireText(request.permissionModule(), "Permission module is required", 255);
        Projects project = findProject(request.projectId());

        boolean duplicateCode = currentId == null
                ? permissionRepository.existsByPermissionCodeIgnoreCase(permissionCode)
                : permissionRepository.existsByPermissionCodeIgnoreCaseAndIdNot(permissionCode, currentId);

        if (duplicateCode) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Permission code already exists");
        }

        permission.setPermissionName(permissionName);
        permission.setPermissionCode(permissionCode);
        permission.setPermissionModule(permissionModule);
        permission.setProject(project);
    }

    private Permissions findPermission(int id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found"));
    }

    private Projects findProject(Integer projectId) {
        if (projectId == null || projectId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project is required");
        }

        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private Pageable createPageable(int page, String sortBy, String sortDirection) {
        int validPage = Math.max(page, 0);
        String sortField = SORT_FIELDS.contains(sortBy) ? sortBy : DEFAULT_SORT_FIELD;

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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, missingMessage);
        }

        if (normalizedValue.length() > maxLength) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Value must not be longer than " + maxLength + " characters"
            );
        }

        return normalizedValue;
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
                permission.getPermissionModule(),
                project == null ? null : project.getId(),
                project == null ? null : project.getProjectName()
        );
    }

    private PermissionDetailResponse toDetail(Permissions permission) {
        Projects project = permission.getProject();

        return new PermissionDetailResponse(
                permission.getId(),
                permission.getPermissionName(),
                permission.getPermissionCode(),
                permission.getPermissionModule(),
                project == null ? null : project.getId(),
                project == null ? null : project.getProjectName()
        );
    }
}
