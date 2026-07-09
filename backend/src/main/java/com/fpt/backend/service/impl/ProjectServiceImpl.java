package com.fpt.backend.service.impl;

import com.fpt.backend.dto.request.project.ProjectCreateRequest;
import com.fpt.backend.dto.request.project.ProjectListRequest;
import com.fpt.backend.dto.response.project.ProjectContractResponse;
import com.fpt.backend.dto.response.project.ProjectDetailResponse;
import com.fpt.backend.dto.response.project.ProjectEmployeeResponse;
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
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
    private static final String DEFAULT_CREATED_BY = "Admin";
    private static final String DEFAULT_PROJECT_STATUS = "Planning";
    private static final String PROJECT_MEMBER_PERMISSION = "Project Member";
    private static final String PROJECT_PERMISSION_MODULE = "PROJECT";

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
    private final EntityManager entityManager;

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

    @Override
    @Transactional
    public ProjectDetailResponse createProject(ProjectCreateRequest request) {
        Projects project = new Projects();
        List<Integer> employeeIds = request == null ? null : request.employeeIds();
        applyCreateRequest(project, request);

        Projects savedProject = projectRepository.save(project);
        addEmployeesToProject(savedProject, employeeIds);

        return toDetail(savedProject);
    }

    @Override
    public List<ProjectEmployeeResponse> getEmployeesForProjectSelection() {
        return projectRepository.findEmployeesForProjectSelection();
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

    private void applyCreateRequest(Projects project, ProjectCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project information is required");
        }

        String projectName = requireText(request.projectName(), "Project name is required");
        String projectCode = requireText(request.projectCode(), "Project code is required");
        LocalDate startDate = request.projectStartDate();
        LocalDate endDate = request.projectEndDate();

        if (startDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date is required");
        }

        if (endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date is required");
        }

        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

        if (projectRepository.existsByProjectCodeIgnoreCase(projectCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project code already exists");
        }

        project.setProjectName(projectName);
        project.setProjectCode(projectCode);
        project.setProjectStartDate(startDate);
        project.setProjectEndDate(endDate);
        project.setProjectCreatedAt(defaultIfBlank(request.projectCreatedAt(), LocalDate.now().toString()));
        project.setProjectDescription(normalize(request.projectDescription()));
        project.setProjectStatus(defaultIfBlank(request.projectStatus(), DEFAULT_PROJECT_STATUS));
        project.setProjectCreatedBy(DEFAULT_CREATED_BY);
    }

    private String requireText(String value, String message) {
        String normalizedValue = normalize(value);

        if (normalizedValue.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return normalizedValue;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        String normalizedValue = normalize(value);
        return normalizedValue.isBlank() ? defaultValue : normalizedValue;
    }

    private void addEmployeesToProject(Projects project, List<Integer> employeeIds) {
        List<Integer> selectedEmployeeIds = normalizeEmployeeIds(employeeIds);

        if (selectedEmployeeIds.isEmpty()) {
            project.setPermission(new ArrayList<>());
            return;
        }

        Permissions permission = Permissions.builder()
                .permissionName(PROJECT_MEMBER_PERMISSION)
                .permissionCode("PROJECT_MEMBER_" + project.getId())
                .permissionModule(PROJECT_PERMISSION_MODULE)
                .project(project)
                .build();
        entityManager.persist(permission);

        List<UserPermission> userPermissions = new ArrayList<>();

        for (Integer employeeId : selectedEmployeeIds) {
            Users user = entityManager.find(Users.class, employeeId);

            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found with id: " + employeeId);
            }

            UserPermission userPermission = UserPermission.builder()
                    .user(user)
                    .permission(permission)
                    .build();
            entityManager.persist(userPermission);
            userPermissions.add(userPermission);
        }

        permission.setUserPermissions(userPermissions);
        project.setPermission(List.of(permission));
    }

    private List<Integer> normalizeEmployeeIds(List<Integer> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedHashSet<Integer> uniqueIds = new LinkedHashSet<>();

        for (Integer employeeId : employeeIds) {
            if (employeeId != null && employeeId > 0) {
                uniqueIds.add(employeeId);
            }
        }

        return new ArrayList<>(uniqueIds);
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
