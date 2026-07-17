package com.fpt.backend.service.impl.project;

import com.fpt.backend.dto.request.project.ProjectCreateRequest;
import com.fpt.backend.dto.request.project.ProjectListRequest;
import com.fpt.backend.dto.request.project.ProjectMemberRequest;
import com.fpt.backend.dto.request.project.ProjectPhaseRequest;
import com.fpt.backend.dto.request.project.ProjectUpdateRequest;
import com.fpt.backend.dto.response.project.ProjectContractResponse;
import com.fpt.backend.dto.response.project.ProjectDetailResponse;
import com.fpt.backend.dto.response.project.ProjectEmployeeResponse;
import com.fpt.backend.dto.response.project.ProjectListItemResponse;
import com.fpt.backend.dto.response.project.ProjectListResponse;
import com.fpt.backend.dto.response.project.ProjectPermissionOptionResponse;
import com.fpt.backend.dto.response.project.ProjectPhaseResponse;
import com.fpt.backend.dto.response.project.ProjectRoleResponse;
import com.fpt.backend.dto.response.project.ProjectUserResponse;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.ProjectMember;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Role;
import com.fpt.backend.entity.Timeline;
import com.fpt.backend.entity.UserPermission;
import com.fpt.backend.entity.UserRole;
import com.fpt.backend.entity.Users;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.project.ProjectService;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private static final String DEFAULT_PHASE_STATUS = "Planning";
    private static final ZoneId DATABASE_TIME_ZONE = ZoneId.of("UTC");
    private static final ZoneId PROJECT_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
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
        ProjectListRequest validRequest = request == null
                ? new ProjectListRequest("", "", 0, DEFAULT_SORT_FIELD, "desc")
                : request;
        String search = normalize(validRequest.search());
        String status = normalize(validRequest.status());
        Pageable pageable = createPageable(
                validRequest.page(),
                validRequest.sortBy(),
                validRequest.sortDirection()
        );
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
        return toDetail(findProject(id));
    }

    @Override
    @Transactional
    public ProjectDetailResponse createProject(ProjectCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project information is required");
        }

        Projects project = new Projects();
        applyProjectInformation(
                project,
                request.projectName(),
                request.projectCode(),
                request.projectStartDate(),
                request.projectEndDate(),
                request.projectDescription(),
                request.projectStatus(),
                null
        );
        project.setProjectCreatedBy(DEFAULT_CREATED_BY);
        project.setProjectCreatedAt(defaultIfBlank(
                request.projectCreatedAt(),
                LocalDate.now(PROJECT_TIME_ZONE).toString()
        ));

        Projects savedProject = projectRepository.save(project);
        syncPhases(savedProject, request.phases());
        syncMembers(savedProject, request.members(), false);
        entityManager.flush();

        return toDetail(savedProject);
    }

    @Override
    @Transactional
    public ProjectDetailResponse updateProject(int id, ProjectUpdateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project information is required");
        }

        Projects project = findProject(id);
        applyProjectInformation(
                project,
                request.projectName(),
                request.projectCode(),
                request.projectStartDate(),
                request.projectEndDate(),
                request.projectDescription(),
                request.projectStatus(),
                id
        );
        projectRepository.save(project);

        syncPhases(project, request.phases());
        syncMembers(project, request.members(), true);
        entityManager.flush();

        return toDetail(project);
    }

    @Override
    @Transactional
    public void deleteProject(int id) {
        Projects project = findProject(id);

        if (hasProjectDependencies(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Project cannot be deleted because it is being used by contracts, workflows, proposals, or activity logs"
            );
        }

        try {
            for (Timeline phase : findProjectPhases(id)) {
                removePhase(phase);
            }

            for (UserPermission userPermission : findProjectUserPermissions(id)) {
                entityManager.remove(userPermission);
            }

            for (ProjectMember member : findProjectMembers(id)) {
                entityManager.remove(member);
            }

            entityManager.flush();

            for (Permissions permission : findProjectPermissions(id)) {
                entityManager.remove(permission);
            }

            entityManager.flush();
            projectRepository.delete(project);
            projectRepository.flush();
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Project cannot be deleted because related data still exists",
                    exception
            );
        }
    }

    @Override
    public List<ProjectEmployeeResponse> getEmployeesForProjectSelection() {
        List<Users> users = entityManager.createQuery(
                        "SELECT user FROM Users user "
                                + "ORDER BY user.firstName, user.lastName, user.email",
                        Users.class
                )
                .getResultList();
        Map<Integer, List<ProjectRoleResponse>> rolesByUserId = findRolesByUserId();

        return users.stream()
                .map(user -> new ProjectEmployeeResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        rolesByUserId.getOrDefault(user.getId(), List.of()),
                        user.getStatus()
                ))
                .toList();
    }

    @Override
    public List<ProjectRoleResponse> getRolesForProjectMemberFilter() {
        return entityManager.createQuery(
                        "SELECT role FROM Role role "
                                + "WHERE role.roleName IS NOT NULL "
                                + "AND TRIM(role.roleName) <> '' "
                                + "ORDER BY role.roleName, role.id",
                        Role.class
                )
                .getResultList()
                .stream()
                .map(role -> new ProjectRoleResponse(role.getId(), role.getRoleName()))
                .toList();
    }

    private Map<Integer, List<ProjectRoleResponse>> findRolesByUserId() {
        List<UserRole> userRoles = entityManager.createQuery(
                        "SELECT userRole FROM UserRole userRole "
                                + "JOIN FETCH userRole.user user "
                                + "JOIN FETCH userRole.role role "
                                + "ORDER BY user.id, role.roleName, role.id",
                        UserRole.class
                )
                .getResultList();
        Map<Integer, List<ProjectRoleResponse>> rolesByUserId = new LinkedHashMap<>();

        for (UserRole userRole : userRoles) {
            int userId = userRole.getUser().getId();
            Role role = userRole.getRole();
            List<ProjectRoleResponse> roles = rolesByUserId.computeIfAbsent(
                    userId,
                    ignored -> new ArrayList<>()
            );
            boolean roleAlreadyAdded = roles.stream()
                    .anyMatch(existingRole -> existingRole.id() == role.getId());

            if (!roleAlreadyAdded) {
                roles.add(new ProjectRoleResponse(role.getId(), role.getRoleName()));
            }
        }

        return rolesByUserId;
    }

    private Projects findProject(int id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
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
        String sortField = sortBy != null && SORT_FIELDS.contains(sortBy)
                ? sortBy
                : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(validPage, PAGE_SIZE, Sort.by(direction, sortField));
    }

    private void applyProjectInformation(
            Projects project,
            String projectNameValue,
            String projectCodeValue,
            LocalDate startDate,
            LocalDate endDate,
            String descriptionValue,
            String statusValue,
            Integer currentProjectId) {
        String projectName = requireText(projectNameValue, "Project name is required", 50);
        String projectCode = requireText(projectCodeValue, "Project code is required", 50);
        String description = normalize(descriptionValue);
        String status = defaultIfBlank(statusValue, DEFAULT_PROJECT_STATUS);

        validateMaxLength(description, "Project description", 255);
        validateMaxLength(status, "Project status", 50);

        if (startDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date is required");
        }

        if (endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date is required");
        }

        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must not be before start date");
        }

        boolean duplicateCode = currentProjectId == null
                ? projectRepository.existsByProjectCodeIgnoreCase(projectCode)
                : projectRepository.existsByProjectCodeIgnoreCaseAndIdNot(projectCode, currentProjectId);

        if (duplicateCode) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project code already exists");
        }

        project.setProjectName(projectName);
        project.setProjectCode(projectCode);
        project.setProjectStartDate(startDate);
        project.setProjectEndDate(endDate);
        project.setProjectDescription(description);
        project.setProjectStatus(status);
    }

    private void syncPhases(
            Projects project,
            List<ProjectPhaseRequest> phaseRequests) {
        List<ProjectPhaseRequest> requests = phaseRequests == null ? List.of() : phaseRequests;
        validatePhaseSchedule(project, requests);
        Map<Integer, Timeline> existingPhases = new LinkedHashMap<>();

        for (Timeline phase : findProjectPhases(project.getId())) {
            existingPhases.put(phase.getId(), phase);
        }

        for (ProjectPhaseRequest request : requests) {
            if (request == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phase information is required");
            }

            Timeline phase;
            boolean isNewPhase = false;

            if (request.id() != null && request.id() > 0) {
                phase = existingPhases.remove(request.id());

                if (phase == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Phase does not belong to this project"
                    );
                }
            } else {
                phase = new Timeline();
                isNewPhase = true;
            }

            applyPhaseInformation(phase, request, project);

            if (isNewPhase) {
                entityManager.persist(phase);
            }
        }

        for (Timeline removedPhase : existingPhases.values()) {
            removePhase(removedPhase);
        }
    }

    private void validatePhaseSchedule(
            Projects project,
            List<ProjectPhaseRequest> requests) {
        if (requests.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one phase is required to cover the full project timeline"
            );
        }

        LocalDate expectedStartDate = project.getProjectStartDate();

        for (int index = 0; index < requests.size(); index++) {
            ProjectPhaseRequest request = requests.get(index);
            int phaseNumber = index + 1;

            if (request == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Phase " + phaseNumber + " information is required"
                );
            }

            LocalDate startDate = request.startDate();
            LocalDate endDate = request.endDate();

            if (startDate == null || endDate == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Phase " + phaseNumber + " start date and end date are required"
                );
            }

            if (endDate.isBefore(startDate)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Phase " + phaseNumber + " end date must not be before its start date"
                );
            }

            if (startDate.isBefore(project.getProjectStartDate())
                    || endDate.isAfter(project.getProjectEndDate())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Phase " + phaseNumber + " must stay inside the project date range"
                );
            }

            if (!startDate.equals(expectedStartDate)) {
                if (index == 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Phase 1 must start on the project start date "
                                    + project.getProjectStartDate()
                    );
                }

                String problem = startDate.isBefore(expectedStartDate)
                        ? " overlaps the previous phase"
                        : " leaves a gap after the previous phase";
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Phase " + phaseNumber + problem
                                + " and must start on " + expectedStartDate
                );
            }

            expectedStartDate = endDate.plusDays(1);
        }

        ProjectPhaseRequest finalPhase = requests.get(requests.size() - 1);
        if (!finalPhase.endDate().equals(project.getProjectEndDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The final phase must end on the project end date "
                            + project.getProjectEndDate()
            );
        }
    }

    private void applyPhaseInformation(
            Timeline phase,
            ProjectPhaseRequest request,
            Projects project) {
        String title = requireText(request.title(), "Phase title is required", 150);
        String description = normalize(request.description());
        String status = defaultIfBlank(request.status(), DEFAULT_PHASE_STATUS);
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();
        double progress = request.progress() == null ? 0 : request.progress();

        validateMaxLength(description, "Phase description", 500);
        validateMaxLength(status, "Phase status", 30);

        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phase start date and end date are required"
            );
        }

        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phase end date must not be before phase start date"
            );
        }

        if (startDate.isBefore(project.getProjectStartDate())
                || endDate.isAfter(project.getProjectEndDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phase dates must be inside the project date range"
            );
        }

        if (progress < 0 || progress > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phase progress must be between 0 and 100"
            );
        }

        phase.setTitle(title);
        phase.setDescription(description);
        phase.setStartDate(java.sql.Date.valueOf(startDate));
        phase.setEndDate(java.sql.Date.valueOf(endDate));
        phase.setStatus(status);
        phase.setProgress(progress);
        phase.setProject(project);
    }

    private void removePhase(Timeline phase) {
        long taskCount = countByPhase("SELECT COUNT(task) FROM TimelineTask task WHERE task.timeline.id = :phaseId", phase.getId());
        long deliverableCount = countByPhase(
                "SELECT COUNT(deliverable) FROM Deliverable deliverable WHERE deliverable.timeline.id = :phaseId",
                phase.getId()
        );

        if (taskCount > 0 || deliverableCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phase cannot be removed because it has tasks or deliverables"
            );
        }

        entityManager.remove(phase);
    }

    private long countByPhase(String query, int phaseId) {
        return entityManager.createQuery(query, Long.class)
                .setParameter("phaseId", phaseId)
                .getSingleResult();
    }

    private void syncMembers(
            Projects project,
            List<ProjectMemberRequest> memberRequests,
            boolean keepExistingWhenMissing) {
        if (memberRequests == null && keepExistingWhenMissing) {
            return;
        }

        List<ProjectMemberRequest> requests = memberRequests == null ? List.of() : memberRequests;
        Map<Integer, ProjectMemberRequest> requestByUserId = new LinkedHashMap<>();

        for (ProjectMemberRequest request : requests) {
            if (request == null || request.userId() == null || request.userId() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid user is required");
            }

            if (requestByUserId.putIfAbsent(request.userId(), request) != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A user cannot be added to the same project more than once"
                );
            }
        }

        Map<Integer, ProjectMember> existingMemberByUserId = new LinkedHashMap<>();
        for (ProjectMember member : findProjectMembers(project.getId())) {
            int userId = member.getUser().getId();
            ProjectMember duplicate = existingMemberByUserId.putIfAbsent(userId, member);

            if (duplicate != null) {
                entityManager.remove(member);
            }
        }

        for (UserPermission userPermission : findProjectUserPermissions(project.getId())) {
            entityManager.remove(userPermission);
        }

        for (ProjectMemberRequest request : requestByUserId.values()) {
            Users user = entityManager.find(Users.class, request.userId());

            if (user == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + request.userId()
                );
            }

            ProjectMember member = existingMemberByUserId.remove(user.getId());
            if (member == null) {
                member = new ProjectMember();
                member.setProject(project);
                member.setUser(user);
                member.setJoinDate(getCurrentDatabaseDateTime());
                entityManager.persist(member);
            }

            if (request.permissionId() != null) {
                Permissions permission = resolvePermission(project, request.permissionId());
                UserPermission userPermission = new UserPermission();
                userPermission.setUser(user);
                userPermission.setPermission(permission);
                entityManager.persist(userPermission);
            }
        }

        for (ProjectMember removedMember : existingMemberByUserId.values()) {
            entityManager.remove(removedMember);
        }
    }

    private Permissions resolvePermission(Projects project, Integer permissionId) {
        if (permissionId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selected permission is invalid"
            );
        }

        Permissions permission = entityManager.find(Permissions.class, permissionId);

        if (permission == null
                || permission.getProject() == null
                || permission.getProject().getId() != project.getId()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selected permission does not belong to this project"
            );
        }

        return permission;
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
                toProjectPhases(project.getId()),
                toProjectUsers(project.getId()),
                toPermissionOptions(project),
                toProjectContracts(project.getId())
        );
    }

    private List<ProjectPhaseResponse> toProjectPhases(int projectId) {
        return findProjectPhases(projectId)
                .stream()
                .map(phase -> new ProjectPhaseResponse(
                        phase.getId(),
                        phase.getTitle(),
                        phase.getDescription(),
                        toLocalDate(phase.getStartDate()),
                        toLocalDate(phase.getEndDate()),
                        phase.getStatus(),
                        phase.getProgress()
                ))
                .toList();
    }

    private List<ProjectUserResponse> toProjectUsers(int projectId) {
        Map<Integer, ProjectMember> memberByUserId = new LinkedHashMap<>();
        Map<Integer, Users> userById = new LinkedHashMap<>();
        Map<Integer, Permissions> permissionByUserId = new LinkedHashMap<>();

        for (ProjectMember member : findProjectMembers(projectId)) {
            Users user = member.getUser();
            memberByUserId.putIfAbsent(user.getId(), member);
            userById.putIfAbsent(user.getId(), user);
        }

        for (UserPermission userPermission : findProjectUserPermissions(projectId)) {
            Users user = userPermission.getUser();
            userById.putIfAbsent(user.getId(), user);
            permissionByUserId.putIfAbsent(user.getId(), userPermission.getPermission());
        }

        List<ProjectUserResponse> users = new ArrayList<>();

        for (Users user : userById.values()) {
            ProjectMember member = memberByUserId.get(user.getId());
            Permissions permission = permissionByUserId.get(user.getId());
            users.add(new ProjectUserResponse(
                    user.getId(),
                    user.getEmail(),
                    getUserName(user),
                    user.getRole(),
                    user.getStatus(),
                    member == null ? null : toProjectJoinDate(member.getJoinDate()),
                    permission == null ? null : permission.getId(),
                    permission == null ? "Not assigned" : getPermissionName(permission),
                    permission == null ? null : permission.getPermissionCode()
            ));
        }

        users.sort(Comparator.comparing(
                ProjectUserResponse::userName,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        ));
        return users;
    }

    private List<ProjectPermissionOptionResponse> toPermissionOptions(Projects project) {
        return findProjectPermissions(project.getId())
                .stream()
                .sorted(Comparator.comparing(
                        this::getPermissionName,
                        String.CASE_INSENSITIVE_ORDER
                ))
                .map(permission -> new ProjectPermissionOptionResponse(
                        permission.getId(),
                        getPermissionName(permission),
                        permission.getPermissionCode(),
                        permission.getPermissionDescription(),
                        permission.getStatus()
                ))
                .toList();
    }

    private List<ProjectContractResponse> toProjectContracts(int projectId) {
        return entityManager.createQuery(
                        "SELECT contract FROM Contracts contract "
                                + "WHERE contract.project.id = :projectId ORDER BY contract.id",
                        Contracts.class
                )
                .setParameter("projectId", projectId)
                .getResultList()
                .stream()
                .map(contract -> new ProjectContractResponse(
                        contract.getContractTitle(),
                        contract.getContractNumber(),
                        contract.getContractStatus()
                ))
                .toList();
    }

    private List<Timeline> findProjectPhases(int projectId) {
        return entityManager.createQuery(
                        "SELECT phase FROM Timeline phase "
                                + "WHERE phase.project.id = :projectId "
                                + "ORDER BY phase.startDate, phase.id",
                        Timeline.class
                )
                .setParameter("projectId", projectId)
                .getResultList();
    }

    private List<ProjectMember> findProjectMembers(int projectId) {
        return entityManager.createQuery(
                        "SELECT member FROM ProjectMember member "
                                + "JOIN FETCH member.user user "
                                + "WHERE member.project.id = :projectId "
                                + "ORDER BY user.firstName, user.lastName, user.email",
                        ProjectMember.class
                )
                .setParameter("projectId", projectId)
                .getResultList();
    }

    private List<UserPermission> findProjectUserPermissions(int projectId) {
        return entityManager.createQuery(
                        "SELECT userPermission FROM UserPermission userPermission "
                                + "JOIN FETCH userPermission.user user "
                                + "JOIN FETCH userPermission.permission permission "
                                + "WHERE permission.project.id = :projectId "
                                + "ORDER BY user.id, permission.id",
                        UserPermission.class
                )
                .setParameter("projectId", projectId)
                .getResultList();
    }

    private List<Permissions> findProjectPermissions(int projectId) {
        return entityManager.createQuery(
                        "SELECT permission FROM Permissions permission "
                                + "WHERE permission.project.id = :projectId "
                                + "ORDER BY permission.id",
                        Permissions.class
                )
                .setParameter("projectId", projectId)
                .getResultList();
    }

    private boolean hasProjectDependencies(int projectId) {
        return countByProject("SELECT COUNT(contract) FROM Contracts contract WHERE contract.project.id = :projectId", projectId) > 0
                || countByProject("SELECT COUNT(log) FROM ActivityLog log WHERE log.project.id = :projectId", projectId) > 0
                || countByProject("SELECT COUNT(workflow) FROM Workflow workflow WHERE workflow.project.id = :projectId", projectId) > 0
                || countByProject("SELECT COUNT(proposal) FROM Proposals proposal WHERE proposal.project.id = :projectId", projectId) > 0;
    }

    private long countByProject(String query, int projectId) {
        return entityManager.createQuery(query, Long.class)
                .setParameter("projectId", projectId)
                .getSingleResult();
    }

    private String getUserName(Users user) {
        String fullName = (normalize(user.getFirstName()) + " " + normalize(user.getLastName())).trim();

        if (!fullName.isBlank()) {
            return fullName;
        }

        String email = normalize(user.getEmail());
        return email.isBlank() ? "User #" + user.getId() : email;
    }

    private String getPermissionName(Permissions permission) {
        String name = normalize(permission.getPermissionName());

        if (!name.isBlank()) {
            return name;
        }

        String code = normalize(permission.getPermissionCode());
        return code.isBlank() ? "Permission #" + permission.getId() : code;
    }

    private LocalDate toLocalDate(Date value) {
        if (value == null) {
            return null;
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        return value.toInstant()
                .atZone(PROJECT_TIME_ZONE)
                .toLocalDate();
    }

    private Date getCurrentDatabaseDateTime() {
        LocalDateTime utcDateTime = LocalDateTime.now(DATABASE_TIME_ZONE);
        return java.sql.Timestamp.valueOf(utcDateTime);
    }

    private LocalDate toProjectJoinDate(Date value) {
        if (value == null) {
            return null;
        }

        LocalDateTime databaseDateTime;
        if (value instanceof java.sql.Timestamp timestamp) {
            databaseDateTime = timestamp.toLocalDateTime();
        } else {
            databaseDateTime = LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
        }

        return databaseDateTime
                .atZone(DATABASE_TIME_ZONE)
                .withZoneSameInstant(PROJECT_TIME_ZONE)
                .toLocalDate();
    }

    private String requireText(String value, String message, int maxLength) {
        String normalizedValue = normalize(value);

        if (normalizedValue.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        validateMaxLength(normalizedValue, message.replace(" is required", ""), maxLength);
        return normalizedValue;
    }

    private void validateMaxLength(String value, String fieldName, int maxLength) {
        if (value.length() > maxLength) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must not be longer than " + maxLength + " characters"
            );
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        String normalizedValue = normalize(value);
        return normalizedValue.isBlank() ? defaultValue : normalizedValue;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
