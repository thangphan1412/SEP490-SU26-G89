package com.fpt.backend.service.impl.project;

import com.fpt.backend.dto.request.project.ProjectCreateRequest;
import com.fpt.backend.dto.request.project.ProjectListRequest;
import com.fpt.backend.dto.request.project.ProjectMemberRequest;
import com.fpt.backend.dto.request.project.ProjectPermissionConfigurationRequest;
import com.fpt.backend.dto.request.project.ProjectUpdateRequest;
import com.fpt.backend.dto.response.project.ProjectContractResponse;
import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.dto.response.project.ProjectDetailResponse;
import com.fpt.backend.dto.response.project.ProjectEmployeeResponse;
import com.fpt.backend.dto.response.project.ProjectListItemResponse;
import com.fpt.backend.dto.response.project.ProjectListResponse;
import com.fpt.backend.dto.response.project.ProjectPermissionConfigurationResponse;
import com.fpt.backend.dto.response.project.ProjectPermissionOptionResponse;
import com.fpt.backend.dto.response.project.ProjectPhaseResponse;
import com.fpt.backend.dto.response.project.ProjectUserResponse;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.ProjectDeleteResult;
import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.project.ProjectCleanupRepository;
import com.fpt.backend.repository.project.ProjectContractRepository;
import com.fpt.backend.repository.project.ProjectMemberRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.permission.IPermissionAccessService;
import com.fpt.backend.service.interfaces.project.IProjectService;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements IProjectService {
    private static final int PAGE_SIZE = 7;
    private static final String NEW_PROJECT_STATUS = "On Hold";
    private static final String CANCELLED_PROJECT_STATUS = "Cancelled";
    private static final String COMPLETED_PROJECT_STATUS = "Completed";
    private static final ZoneId PROJECT_TIME_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");
    private final ProjectRepository projectRepository;
    private final ProjectContractRepository projectContractRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectCleanupRepository projectCleanupRepository;
    private final ProjectPhaseService projectPhaseService;
    private final ProjectMemberService projectMemberService;
    private final ProjectPermissionService projectPermissionService;
    private final ProjectApprovalService projectApprovalService;
    private final IPermissionAccessService permissionAccessService;
    private final CurrentUser currentUserUtil;

    //Load danh sách các dự án dựa trên tìm kiếm, trạng thái dự án, quyền truy cập của người dùng hiện tại và phân trang.
    @Override
    public ProjectListResponse getProjects(ProjectListRequest request) {
        String search = normalize(request.search());
        String status = normalize(request.status());
        Users currentUser = currentUserUtil.getCurrentUser();
        Pageable pageable = createPageable(request.page());
        Page<Projects> projects = findProjects(
                search,
                status,
                request.viewOnlyYourProjects(),
                currentUser.getId(),
                pageable
        );
        List<ProjectListItemResponse> projectItems = new ArrayList<>();

        for (Projects project : projects.getContent()) {
            ProjectListItemResponse projectItem = toListItem(
                    project,
                    currentUser
            );
            projectItems.add(projectItem);
        }

        return new ProjectListResponse(
                projectItems,
                projects.getTotalElements(),
                projects.getTotalPages()
        );
    }

    //Lấy chi tiết dự án dựa trên ID dự án và quyền truy cập của người dùng hiện tại.
    @Override
    @Transactional
    public ProjectDetailResponse getProjectById(UUID id) {
        Projects project = findProject(id);
        ProjectAccessResponse access =
                permissionAccessService.getCurrentUserAccess(id);
        return toDetail(project, access);
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
                null
        );
        project.setProjectStatus(NEW_PROJECT_STATUS);
        project.setProjectCreatedBy(currentUser);
        project.setProjectCreatedAt(
                LocalDate.now(PROJECT_TIME_ZONE).toString()
        );

        Projects savedProject = projectRepository.save(project);
        projectApprovalService.createApprovalRequest(
                savedProject,
                currentUser
        );
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

        return toDetail(
                savedProject,
                permissionAccessService.getCurrentUserAccess(
                        savedProject.getId()
                )
        );
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
        ProjectAccessResponse access =
                permissionAccessService.getCurrentUserAccess(id);

        if (isCompletedProject(project)) {
            throw new BadHttpException(
                    "Completed projects cannot be updated"
            );
        }

        boolean updateProjectInformation = hasProjectInformation(request);
        boolean updatePhases = request.phases() != null;
        boolean updateMembers = request.members() != null;

        if (!updateProjectInformation && !updatePhases && !updateMembers) {
            throw new BadHttpException("No project changes were provided");
        }

        if (updateProjectInformation || updatePhases) {
            permissionAccessService.requireAction(id, "EDIT_PROJECT");
        }

        if (updateProjectInformation) {
            boolean projectDatesChanged = !Objects.equals(
                    project.getProjectStartDate(),
                    request.projectStartDate()
            ) || !Objects.equals(
                    project.getProjectEndDate(),
                    request.projectEndDate()
            );

            if (projectDatesChanged) {
                if (!updatePhases) {
                    throw new BadHttpException(
                            "Phases are required when project dates change"
                    );
                }
            }

            applyProjectInformation(
                    project,
                    request.projectName(),
                    request.projectCode(),
                    request.projectStartDate(),
                    request.projectEndDate(),
                    request.projectDescription(),
                    id
            );
            projectRepository.save(project);
        }

        if (updatePhases) {
            projectPhaseService.syncPhases(project, request.phases());
        }

        if (updateMembers) {
            permissionAccessService.requireAction(id, "MANAGE_MEMBERS");
            projectMemberService.syncMembers(project, request.members(), true);
        }

        projectRepository.flush();

        return toDetail(project, access);
    }

    @Override
    @Transactional
    public void approveProject(UUID id) {
        Projects project = findProject(id);
        Users currentUser = currentUserUtil.getCurrentUser();
        projectApprovalService.approveProject(project, currentUser);
        projectRepository.flush();
    }

    @Override
    @Transactional
    public ProjectDeleteResult deleteProject(UUID id) {
        Projects project = findProject(id);
        permissionAccessService.requireAction(id, "EDIT_PROJECT");

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

    //Lấy enum danh sách trạng thái người dùng để lọc thành viên dự án.
    @Override
    public List<UserStatus> getUserStatusesForProjectMemberFilter() {
        return List.of(UserStatus.values());
    }

    @Override
    public List<ProjectPermissionConfigurationResponse>
    getProjectPermissionConfigurations(UUID projectId) {
        findProject(projectId);
        permissionAccessService.requireAction(projectId, "MANAGE_MEMBERS");
        return projectPermissionService.getConfigurations(projectId);
    }

    @Override
    @Transactional
    public ProjectPermissionConfigurationResponse configureProjectPermission(
            UUID projectId,
            UUID permissionId,
            ProjectPermissionConfigurationRequest request) {
        Projects project = findProject(projectId);
        permissionAccessService.requireAction(projectId, "MANAGE_MEMBERS");
        return projectPermissionService.configure(
                project,
                permissionId,
                request
        );
    }

    //Tìm kiếm dự án dựa trên ID dự án. Nếu không tìm thấy, ném NotFoundException.
    private Projects findProject(UUID id) {
        Optional<Projects> project = projectRepository.findById(id);

        if (project.isEmpty()) {
            throw new NotFoundException("Project not found");
        }

        return project.get();
    }

    //Tìm kiếm các dự án dựa trên tìm kiếm theo từ khóa, trạng thái dự án, quyền truy cập của người dùng hiện tại và phân trang.
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

    private Pageable createPageable(int page) {
        int validPage = Math.max(page, 0);
        Sort newestProjectFirst = Sort.by(
                Sort.Direction.DESC,
                "projectCreatedAt"
        );

        return PageRequest.of(validPage, PAGE_SIZE, newestProjectFirst);
    }

    private void applyProjectInformation(
            Projects project,
            String projectNameValue,
            String projectCodeValue,
            LocalDate startDate,
            LocalDate endDate,
            String descriptionValue,
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

        validateMaxLength(description, "Project description", 255);
        validateProjectDateRange(startDate, endDate);

        if (currentProjectId == null
                && endDate.isBefore(LocalDate.now(PROJECT_TIME_ZONE))) {
            throw new BadHttpException(
                    "Project end date must not be before today"
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
    }

    private void validateProjectDateRange(
            LocalDate startDate,
            LocalDate endDate) {
        if (startDate == null) {
            throw new BadHttpException("Start date is required");
        }

        if (endDate == null) {
            throw new BadHttpException("End date is required");
        }

        if (startDate.isAfter(endDate)) {
            throw new BadHttpException(
                    "Start date must not be after end date"
            );
        }
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
        boolean projectMember = projectMemberRepository
                .countByProjectIdAndUserId(
                        project.getId(),
                        currentUser.getId()
                ) > 0;
        boolean canView = projectMember
                || projectApprovalService.canReviewProjects(currentUser);
        boolean canApprove = projectApprovalService
                .canApproveProject(project, currentUser);

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
                canView,
                canApprove
        );
    }

    private ProjectDetailResponse toDetail(
            Projects project,
            ProjectAccessResponse access) {
        UUID projectId = project.getId();
        boolean canManageMembers = permissionAccessService.hasAction(
                access,
                "MANAGE_MEMBERS"
        );
        boolean canViewMembers = canManageMembers
                || access.canViewAllProjectData();
        boolean canViewContracts = permissionAccessService.hasAction(
                access,
                "VIEW_CONTRACTS"
        );
        List<ProjectPhaseResponse> phases =
                projectPhaseService.getProjectPhases(projectId);
        List<ProjectUserResponse> users = List.of();
        List<ProjectPermissionOptionResponse> permissionOptions = List.of();
        List<ProjectContractResponse> contracts = List.of();

        if (canViewMembers) {
            users = projectMemberService.getProjectUsers(projectId);
        }

        if (canManageMembers) {
            permissionOptions = projectPermissionService.getOptions(projectId);
        }

        if (canViewContracts) {
            contracts = toProjectContracts(projectId);
        }

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
                phases,
                users,
                permissionOptions,
                contracts,
                access
        );
    }

    private boolean hasProjectInformation(ProjectUpdateRequest request) {
        return request.projectName() != null
                || request.projectCode() != null
                || request.projectStartDate() != null
                || request.projectEndDate() != null
                || request.projectDescription() != null;
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

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
