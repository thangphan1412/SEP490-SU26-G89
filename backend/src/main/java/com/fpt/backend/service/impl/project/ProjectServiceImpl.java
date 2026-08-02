package com.fpt.backend.service.impl.project;

import com.fpt.backend.dto.request.project.ProjectCreateRequest;
import com.fpt.backend.dto.request.project.ProjectListRequest;
import com.fpt.backend.dto.request.project.ProjectMemberRequest;
import com.fpt.backend.dto.request.project.ProjectPermissionConfigurationRequest;
import com.fpt.backend.dto.request.project.ProjectUpdateRequest;
import com.fpt.backend.dto.response.project.ProjectContractResponse;
import com.fpt.backend.dto.response.project.ProjectDetailResponse;
import com.fpt.backend.dto.response.project.ProjectEmployeeResponse;
import com.fpt.backend.dto.response.project.ProjectListItemResponse;
import com.fpt.backend.dto.response.project.ProjectListResponse;
import com.fpt.backend.dto.response.project.ProjectPermissionConfigurationResponse;
import com.fpt.backend.dto.response.project.ProjectRoleResponse;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.project.ProjectCleanupRepository;
import com.fpt.backend.repository.project.ProjectContractRepository;
import com.fpt.backend.repository.project.ProjectMemberRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.project.ProjectDeleteResult;
import com.fpt.backend.service.interfaces.project.ProjectMemberService;
import com.fpt.backend.service.interfaces.project.ProjectPermissionService;
import com.fpt.backend.service.interfaces.project.ProjectPhaseService;
import com.fpt.backend.service.interfaces.project.ProjectService;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {
    private static final int PAGE_SIZE = 7;
    private static final String DATA_SOURCE = "DATABASE";
    private static final String DEFAULT_SORT_FIELD = "projectCreatedAt";
    private static final String DEFAULT_PROJECT_STATUS = "Planning";
    private static final String CANCELLED_PROJECT_STATUS = "Cancelled";
    private static final String COMPLETED_PROJECT_STATUS = "Completed";
    private static final String PROJECT_ACCESS_DENIED_MESSAGE =
            "Bạn không được quyền xem project này!";
    private static final List<String> CREATE_PROJECT_STATUSES = List.of(
            "Planning",
            "Active",
            "On Hold"
    );
    private static final ZoneId PROJECT_TIME_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");
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
    private final ProjectContractRepository projectContractRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectCleanupRepository projectCleanupRepository;
    private final ProjectPhaseService projectPhaseService;
    private final ProjectMemberService projectMemberService;
    private final ProjectPermissionService projectPermissionService;
    private final CurrentUser currentUserUtil;

    @Override
    public ProjectListResponse getProjects(ProjectListRequest request) {
        ProjectListRequest validRequest = request;

        if (validRequest == null) {
            validRequest = new ProjectListRequest(
                    "",
                    "",
                    false,
                    0,
                    DEFAULT_SORT_FIELD,
                    "desc"
            );
        }

        String search = normalize(validRequest.search());
        String status = normalize(validRequest.status());
        Users currentUser = currentUserUtil.getCurrentUser();
        Pageable pageable = createPageable(
                validRequest.page(),
                validRequest.sortBy(),
                validRequest.sortDirection()
        );
        Page<Projects> projects = findProjects(
                search,
                status,
                validRequest.viewOnlyYourProjects(),
                currentUser.getId(),
                pageable
        );

        return new ProjectListResponse(
                DATA_SOURCE,
                projects.map(project -> toListItem(project, currentUser))
                        .getContent(),
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
    public ProjectDetailResponse getProjectById(UUID id) {
        Projects project = findProject(id);
        Users currentUser = currentUserUtil.getCurrentUser();
        boolean currentUserIsCreator =
                isProjectCreator(project, currentUser);
        boolean currentUserIsMember =
                projectMemberRepository.countByProjectIdAndUserId(
                        project.getId(),
                        currentUser.getId()
                ) > 0;

        if (!currentUserIsCreator && !currentUserIsMember) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    PROJECT_ACCESS_DENIED_MESSAGE
            );
        }

        return toDetail(project, currentUserIsCreator);
    }

    @Override
    @Transactional
    public ProjectDetailResponse createProject(ProjectCreateRequest request) {
        if (request == null) {
            throw new BadHttpException("Project information is required");
        }

        Users currentUser = currentUserUtil.getCurrentUser();
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
        project.setProjectCreatedBy(currentUser);
        project.setProjectCreatedAt(
                LocalDate.now(PROJECT_TIME_ZONE).toString()
        );

        Projects savedProject = projectRepository.save(project);
        UUID fullAccessPermissionId =
                projectPermissionService.createProjectFullAccessPermission(
                        savedProject
                );
        projectPhaseService.syncPhases(savedProject, request.phases());
        projectMemberService.syncMembers(
                savedProject,
                createInitialMembers(
                        request.members(),
                        currentUser.getId(),
                        fullAccessPermissionId
                ),
                false
        );
        projectRepository.flush();

        return toDetail(savedProject, true);
    }

    @Override
    @Transactional
    public ProjectDetailResponse updateProject(
            UUID id,
            ProjectUpdateRequest request) {
        if (request == null) {
            throw new BadHttpException("Project information is required");
        }

        Projects project = findProject(id);

        if (isCompletedProject(project)) {
            throw new BadHttpException(
                    "Completed projects cannot be updated"
            );
        }

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

        projectPhaseService.syncPhases(project, request.phases());
        projectMemberService.syncMembers(project, request.members(), true);
        projectRepository.flush();

        return toDetail(
                project,
                isProjectCreator(project, currentUserUtil.getCurrentUser())
        );
    }

    @Override
    @Transactional
    public ProjectDeleteResult deleteProject(UUID id) {
        Projects project = findProject(id);

        if (isCompletedProject(project)) {
            throw new BadHttpException(
                    "Completed projects cannot be deleted"
            );
        }

        if (projectContractRepository.countByProjectId(id) > 0) {
            project.setProjectStatus(CANCELLED_PROJECT_STATUS);
            projectRepository.save(project);
            projectRepository.flush();
            return ProjectDeleteResult.STATUS_CHANGED_TO_CANCELLED;
        }

        try {
            projectPhaseService.deleteProjectData(id);
            projectMemberService.deleteProjectData(id);
            projectPermissionService.deleteProjectData(id);
            projectCleanupRepository.deleteProjectRecords(id);
            projectRepository.delete(project);
            projectRepository.flush();
            return ProjectDeleteResult.DELETED_PERMANENTLY;
        } catch (DataIntegrityViolationException exception) {
            throw new BadHttpException(
                    "Project cannot be deleted because related data still exists"
            );
        }
    }

    @Override
    public List<ProjectEmployeeResponse> getEmployeesForProjectSelection() {
        return projectMemberService.getEmployeesForSelection();
    }

    @Override
    public List<ProjectRoleResponse> getRolesForProjectMemberFilter() {
        return projectMemberService.getRolesForFilter();
    }

    @Override
    public List<ProjectPermissionConfigurationResponse>
    getProjectPermissionConfigurations(UUID projectId) {
        findProject(projectId);
        return projectPermissionService.getConfigurations(projectId);
    }

    @Override
    @Transactional
    public ProjectPermissionConfigurationResponse configureProjectPermission(
            UUID projectId,
            UUID permissionId,
            ProjectPermissionConfigurationRequest request) {
        Projects project = findProject(projectId);
        return projectPermissionService.configure(
                project,
                permissionId,
                request
        );
    }

    private Projects findProject(UUID id) {
        Optional<Projects> project = projectRepository.findById(id);

        if (project.isEmpty()) {
            throw new NotFoundException("Project not found");
        }

        return project.get();
    }

    private Page<Projects> findProjects(
            String search,
            String status,
            boolean viewOnlyYourProjects,
            UUID currentUserId,
            Pageable pageable) {
        if (viewOnlyYourProjects) {
            return projectRepository.searchViewableProjects(
                    search.toLowerCase(Locale.ROOT),
                    status.toLowerCase(Locale.ROOT),
                    currentUserId,
                    pageable
            );
        }

        if (search.isBlank() && status.isBlank()) {
            return projectRepository.findAll(pageable);
        }

        if (search.isBlank()) {
            return projectRepository.findByProjectStatusIgnoreCase(
                    status,
                    pageable
            );
        }

        return projectRepository.searchProjects(
                search.toLowerCase(Locale.ROOT),
                status.toLowerCase(Locale.ROOT),
                pageable
        );
    }

    private Pageable createPageable(
            int page,
            String sortBy,
            String sortDirection) {
        int validPage = Math.max(page, 0);
        String sortField = DEFAULT_SORT_FIELD;

        if (sortBy != null && SORT_FIELDS.contains(sortBy)) {
            sortField = sortBy;
        }

        Sort.Direction direction = Sort.Direction.DESC;

        if ("asc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.ASC;
        }

        Sort sort = Sort.by(direction, sortField);

        if ("projectCreatedBy".equals(sortField)) {
            sort = Sort.by(direction, "projectCreatedBy.firstName")
                    .and(Sort.by(
                            direction,
                            "projectCreatedBy.lastName"
                    ));
        }

        return PageRequest.of(validPage, PAGE_SIZE, sort);
    }

    private void applyProjectInformation(
            Projects project,
            String projectNameValue,
            String projectCodeValue,
            LocalDate startDate,
            LocalDate endDate,
            String descriptionValue,
            String statusValue,
            UUID currentProjectId) {
        String projectName = requireText(
                projectNameValue,
                "Project name is required",
                50
        );
        String projectCode = requireText(
                projectCodeValue,
                "Project code is required",
                50
        );
        String description = normalize(descriptionValue);
        String status = defaultIfBlank(
                statusValue,
                DEFAULT_PROJECT_STATUS
        );

        if (currentProjectId == null) {
            status = getCreateProjectStatus(status);
        }

        validateMaxLength(description, "Project description", 255);
        validateMaxLength(status, "Project status", 50);

        if (startDate == null) {
            throw new BadHttpException("Start date is required");
        }

        if (endDate == null) {
            throw new BadHttpException("End date is required");
        }

        if (endDate.isBefore(startDate)) {
            throw new BadHttpException(
                    "End date must not be before start date"
            );
        }

        boolean duplicateCode;

        if (currentProjectId == null) {
            duplicateCode = projectRepository
                    .existsByProjectCodeIgnoreCase(projectCode);
        } else {
            duplicateCode = projectRepository
                    .existsByProjectCodeIgnoreCaseAndIdNot(
                            projectCode,
                            currentProjectId
                    );
        }

        if (duplicateCode) {
            throw new BadHttpException("Project code already exists");
        }

        project.setProjectName(projectName);
        project.setProjectCode(projectCode);
        project.setProjectStartDate(startDate);
        project.setProjectEndDate(endDate);
        project.setProjectDescription(description);
        project.setProjectStatus(status);
    }

    private String getCreateProjectStatus(String status) {
        for (String allowedStatus : CREATE_PROJECT_STATUSES) {
            if (allowedStatus.equalsIgnoreCase(status)) {
                return allowedStatus;
            }
        }

        throw new BadHttpException(
                "Project status must be Planning, Active, or On Hold "
                        + "when creating a project"
        );
    }

    private boolean isCompletedProject(Projects project) {
        String status = normalize(project.getProjectStatus());
        return COMPLETED_PROJECT_STATUS.equalsIgnoreCase(status);
    }

    private List<ProjectMemberRequest> createInitialMembers(
            List<ProjectMemberRequest> requestedMembers,
            UUID projectCreatorId,
            UUID fullAccessPermissionId) {
        List<ProjectMemberRequest> initialMembers = new ArrayList<>();

        if (requestedMembers != null) {
            for (ProjectMemberRequest member : requestedMembers) {
                if (member != null
                        && projectCreatorId.equals(member.userId())) {
                    continue;
                }

                initialMembers.add(member);
            }
        }

        initialMembers.add(new ProjectMemberRequest(
                projectCreatorId,
                fullAccessPermissionId
        ));
        return initialMembers;
    }

    private ProjectListItemResponse toListItem(
            Projects project,
            Users currentUser) {
        boolean canView = isProjectCreator(project, currentUser)
                || projectMemberRepository.countByProjectIdAndUserId(
                        project.getId(),
                        currentUser.getId()
                ) > 0;

        return new ProjectListItemResponse(
                project.getId(),
                project.getProjectCode(),
                project.getProjectName(),
                project.getProjectDescription(),
                project.getProjectStatus(),
                project.getProjectStartDate(),
                project.getProjectEndDate(),
                getUserName(project.getProjectCreatedBy()),
                project.getProjectCreatedAt(),
                canView
        );
    }

    private ProjectDetailResponse toDetail(
            Projects project,
            boolean currentUserIsCreator) {
        UUID projectId = project.getId();

        return new ProjectDetailResponse(
                projectId,
                project.getProjectCode(),
                project.getProjectName(),
                project.getProjectDescription(),
                project.getProjectStatus(),
                project.getProjectStartDate(),
                project.getProjectEndDate(),
                getUserName(project.getProjectCreatedBy()),
                project.getProjectCreatedAt(),
                projectPhaseService.getProjectPhases(projectId),
                projectMemberService.getProjectUsers(projectId),
                projectPermissionService.getOptions(projectId),
                toProjectContracts(projectId),
                currentUserIsCreator
        );
    }

    private List<ProjectContractResponse> toProjectContracts(UUID projectId) {
        List<Contracts> contracts =
                projectContractRepository.findByProjectId(projectId);
        List<ProjectContractResponse> responses = new ArrayList<>();

        for (Contracts contract : contracts) {
            responses.add(new ProjectContractResponse(
                    contract.getId(),
                    contract.getContractTitle(),
                    contract.getContractNumber(),
                    contract.getContractStatus()
            ));
        }

        return responses;
    }

    private boolean isProjectCreator(
            Projects project,
            Users currentUser) {
        Users projectCreator = project.getProjectCreatedBy();

        return projectCreator != null
                && projectCreator.getId().equals(currentUser.getId());
    }

    private String getUserName(Users currentUser) {
        String firstName = normalize(currentUser.getFirstName());
        String lastName = normalize(currentUser.getLastName());
        String fullName = (firstName + " " + lastName).trim();

        if (!fullName.isBlank()) {
            validateMaxLength(fullName, "Project creator", 50);
            return fullName;
        }

        String email = normalize(currentUser.getEmail());

        if (email.isBlank()) {
            throw new BadHttpException(
                    "Authenticated user information is missing"
            );
        }

        validateMaxLength(email, "Project creator", 50);
        return email;
    }

    private String requireText(
            String value,
            String message,
            int maxLength) {
        String normalizedValue = normalize(value);

        if (normalizedValue.isBlank()) {
            throw new BadHttpException(message);
        }

        validateMaxLength(
                normalizedValue,
                message.replace(" is required", ""),
                maxLength
        );
        return normalizedValue;
    }

    private void validateMaxLength(
            String value,
            String fieldName,
            int maxLength) {
        if (value.length() > maxLength) {
            throw new BadHttpException(
                    fieldName + " must not be longer than "
                            + maxLength + " characters"
            );
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        String normalizedValue = normalize(value);

        if (normalizedValue.isBlank()) {
            return defaultValue;
        }

        return normalizedValue;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
