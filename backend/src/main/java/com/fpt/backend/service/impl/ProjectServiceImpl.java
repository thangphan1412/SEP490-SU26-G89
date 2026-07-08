package com.fpt.backend.service.impl;

import com.fpt.backend.dto.request.project.ProjectListRequest;
import com.fpt.backend.dto.response.project.ProjectContractResponse;
import com.fpt.backend.dto.response.project.ProjectDetailResponse;
import com.fpt.backend.dto.response.project.ProjectListItemResponse;
import com.fpt.backend.dto.response.project.ProjectListResponse;
import com.fpt.backend.dto.response.project.ProjectUserResponse;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.UserPermission;
import com.fpt.backend.entity.Users;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {
    private static final int PAGE_SIZE = 7;
    private static final String DATA_SOURCE = "DATABASE";
    private static final String DEFAULT_SORT_FIELD = "id";

    private static final Set<String> SORT_FIELDS = Set.of(
            "id",
            "projectCode",
            "projectName",
            "projectStatus",
            "projectStartDate",
            "projectEndDate",
            "projectCreatedBy",
            "projectCreatedAt"
    );

    private final ProjectRepository projectRepository;

    @Override
    public ProjectListResponse getProjects(ProjectListRequest request) {
        String search = normalize(request.search());
        String status = normalize(request.status());
        Pageable pageable = createPageable(request.page(), request.sortBy(), request.sortDirection());

        Page<Projects> projects = findProjects(search, status, pageable);

        return new ProjectListResponse(
                DATA_SOURCE,
                projects.map(this::toListItem).getContent(),
                projects.getNumber(),
                projects.getSize(),
                projects.getTotalElements(),
                projects.getTotalPages(),
                projects.isFirst(),
                projects.isLast(),
                projectRepository.findDistinctProjectStatuses()
        );
    }

    @Override
    public ProjectDetailResponse getProjectById(int id) {
        Projects project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        return toDetail(project);
    }

    private Page<Projects> findProjects(String search, String status, Pageable pageable) {
        if (search.isBlank() && status.isBlank()) {
            return projectRepository.findAll(pageable);
        }

        if (search.isBlank()) {
            return projectRepository.findByProjectStatusIgnoreCase(status, pageable);
        }

        return projectRepository.searchProjects(
                search.toLowerCase(Locale.ROOT),
                status.toLowerCase(Locale.ROOT),
                pageable
        );
    }

    private Pageable createPageable(int page, String sortBy, String sortDirection) {
        int validPage = Math.max(page, 0);
        String sortField = sortBy != null && SORT_FIELDS.contains(sortBy) ? sortBy : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(validPage, PAGE_SIZE, Sort.by(direction, sortField));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private ProjectListItemResponse toListItem(Projects project) {
        return new ProjectListItemResponse(
                project.getId(),
                project.getProjectCode(),
                project.getProjectName(),
                project.getProjectDescription(),
                project.getProjectStatus(),
                project.getProjectStartDate(),
                project.getProjectEndDate(),
                project.getProjectCreatedBy(),
                project.getProjectCreatedAt()
        );
    }

    private ProjectDetailResponse toDetail(Projects project) {
        return new ProjectDetailResponse(
                project.getId(),
                project.getProjectCode(),
                project.getProjectName(),
                project.getProjectDescription(),
                project.getProjectStatus(),
                project.getProjectStartDate(),
                project.getProjectEndDate(),
                project.getProjectCreatedBy(),
                project.getProjectCreatedAt(),
                toProjectUsers(project),
                toProjectContracts(project)
        );
    }

    private List<ProjectUserResponse> toProjectUsers(Projects project) {
        List<ProjectUserResponse> users = new ArrayList<>();

        if (project.getPermission() == null) {
            return users;
        }

        for (Permissions permission : project.getPermission()) {
            if (permission.getUserPermissions() == null) {
                continue;
            }

            for (UserPermission userPermission : permission.getUserPermissions()) {
                Users user = userPermission.getUser();

                if (user == null) {
                    continue;
                }

                users.add(new ProjectUserResponse(
                        getUserName(user),
                        getPermissionName(permission)
                ));
            }
        }

        return users;
    }

    private List<ProjectContractResponse> toProjectContracts(Projects project) {
        List<ProjectContractResponse> contracts = new ArrayList<>();

        if (project.getContract() == null) {
            return contracts;
        }

        for (Contracts contract : project.getContract()) {
            contracts.add(new ProjectContractResponse(
                    contract.getContractTitle(),
                    contract.getContractNumber(),
                    contract.getContractStatus()
            ));
        }

        return contracts;
    }

    private String getUserName(Users user) {
        String fullName = (normalize(user.getFirstName()) + " " + normalize(user.getLastName())).trim();

        if (!fullName.isBlank()) {
            return fullName;
        }

        String email = normalize(user.getEmail());

        if (!email.isBlank()) {
            return email;
        }

        return "User #" + user.getId();
    }

    private String getPermissionName(Permissions permission) {
        String permissionName = normalize(permission.getPermissionName());

        if (!permissionName.isBlank()) {
            return permissionName;
        }

        String permissionCode = normalize(permission.getPermissionCode());

        if (!permissionCode.isBlank()) {
            return permissionCode;
        }

        String permissionModule = normalize(permission.getPermissionModule());

        if (!permissionModule.isBlank()) {
            return permissionModule;
        }

        return "-";
    }
}
