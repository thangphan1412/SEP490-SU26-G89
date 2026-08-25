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
import com.fpt.backend.repository.user.UserRepository;
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
    private final UserRepository userRepository;
    private final ProjectCleanupRepository projectCleanupRepository;
    private final ProjectPhaseService projectPhaseService;
    private final ProjectMemberService projectMemberService;
    private final ProjectPermissionService projectPermissionService;
    private final ProjectApprovalService projectApprovalService;
    private final IPermissionAccessService permissionAccessService;
    private final CurrentUser currentUserUtil;

    // Lấy danh sách dự án theo bộ lọc, phạm vi truy cập và phân trang.
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
            ProjectListItemResponse projectItem = toListItem(project,currentUser);
            projectItems.add(projectItem);
        }

        return new ProjectListResponse(
                projectItems,
                projects.getTotalElements(),
                projects.getTotalPages()
        );
    }

    // Lấy chi tiết dự án sau khi xác minh quyền truy cập của người dùng hiện tại.
    @Override
    @Transactional
    public ProjectDetailResponse getProjectById(UUID id) {
        Projects project = findProject(id);
        ProjectAccessResponse access =
                permissionAccessService.getCurrentUserAccess(id);
        return toDetail(project, access);
    }

    // Tạo dự án cùng yêu cầu phê duyệt, phase, quyền mặc định và thành viên ban đầu.
    @Override
    @Transactional
    public ProjectDetailResponse createProject(ProjectCreateRequest request) {
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
        // Phase là tùy chọn khi tạo dự án.
        if (request.phases() != null && !request.phases().isEmpty()) {
            projectPhaseService.syncPhases(savedProject, request.phases());
        }
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

    // Cập nhật các phần thông tin, phase và thành viên được cung cấp của dự án.
    @Override
    @Transactional
    public ProjectDetailResponse updateProject(
            UUID id,
            ProjectUpdateRequest request) {
        Projects project = findProject(id);
        ProjectAccessResponse access =
                permissionAccessService.getCurrentUserAccess(id);

        // Không cho phép thay đổi dự án đã hoàn thành.
        if (isCompletedProject(project)) {
            throw new BadHttpException(
                    "Completed projects cannot be updated"
            );
        }

        boolean updateProjectInformation = hasProjectInformation(request);
        boolean updatePhases = request.phases() != null;
        boolean updateMembers = request.members() != null;

        // Các trường thông tin cơ bản phải được gửi cùng nhau khi cập nhật.
        if (updateProjectInformation
                && !hasCompleteProjectInformation(request)) {
            throw new BadHttpException(
                    "Project name, code, start date and end date must be provided together"
            );
        }

        // Từ chối request không chứa bất kỳ thay đổi nào.
        if (!updateProjectInformation && !updatePhases && !updateMembers) {
            throw new BadHttpException("No project changes were provided");
        }

        // Yêu cầu action chỉnh sửa dự án khi thay đổi thông tin hoặc phase.
        if (updateProjectInformation || updatePhases) {
            permissionAccessService.requireAction(id, "EDIT_PROJECT");
        }

        // Áp dụng các trường thông tin dự án khi request có cung cấp.
        if (updateProjectInformation) {
            boolean projectDatesChanged = !Objects.equals(
                    project.getProjectStartDate(),
                    request.projectStartDate()
            ) || !Objects.equals(
                    project.getProjectEndDate(),
                    request.projectEndDate()
            );

            // Yêu cầu gửi lại phase khi timeline dự án thay đổi.
            if (projectDatesChanged) {
                // Từ chối đổi timeline dự án khi request không kèm danh sách phase mới.
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

        // Đồng bộ phase khi request có danh sách phase.
        if (updatePhases) {
            projectPhaseService.syncPhases(project, request.phases());
        }

        // Đồng bộ thành viên sau khi kiểm tra action quản lý thành viên.
        if (updateMembers) {
            permissionAccessService.requireAction(id, "MANAGE_MEMBERS");
            projectMemberService.syncMembers(project, request.members(), true);
        }

        projectRepository.flush();

        return toDetail(project, access);
    }

    // Ghi nhận lượt phê duyệt dự án của người dùng hiện tại.
    @Override
    @Transactional
    public void approveProject(UUID id) {
        Projects project = findProject(id);
        Users currentUser = currentUserUtil.getCurrentUser();
        projectApprovalService.approveProject(project, currentUser);
        projectRepository.flush();
    }

    // Xóa dự án chưa có hợp đồng hoặc chuyển sang Cancelled khi đã có hợp đồng.
    @Override
    @Transactional
    public ProjectDeleteResult deleteProject(UUID id) {
        Projects project = findProject(id);
        permissionAccessService.requireAction(id, "EDIT_PROJECT");

        // Không cho phép xóa dự án đã hoàn thành.
        if (isCompletedProject(project)) {
            throw new BadHttpException(
                    "Completed projects cannot be deleted"
            );
        }

        // Giữ dữ liệu và chuyển trạng thái khi dự án đã phát sinh hợp đồng.
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

    // Lấy danh sách nhân viên có thể chọn làm thành viên dự án.
    @Override
    public List<ProjectEmployeeResponse> getEmployeesForProjectSelection() {
        List<Users> users = userRepository.findAll(
                Sort.by(
                        Sort.Direction.ASC,
                        "firstName",
                        "lastName",
                        "email"
                )
        );
        List<ProjectEmployeeResponse> employees = new ArrayList<>();

        for (Users user : users) {
            employees.add(new ProjectEmployeeResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getStatus()
            ));
        }

        return employees;
    }

    // Lấy toàn bộ trạng thái người dùng dùng để lọc thành viên dự án.
    @Override
    public List<UserStatus> getUserStatusesForProjectMemberFilter() {
        return List.of(UserStatus.values());
    }

    // Lấy cấu hình quyền của dự án sau khi xác minh action quản lý thành viên.
    @Override
    public List<ProjectPermissionConfigurationResponse>
    getProjectPermissionConfigurations(UUID projectId) {
        findProject(projectId);
        permissionAccessService.requireAction(projectId, "MANAGE_MEMBERS");
        return projectPermissionService.getConfigurations(projectId);
    }

    // Cập nhật một cấu hình quyền thuộc dự án.
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

    // Tìm dự án theo mã định danh hoặc báo không tìm thấy.
    private Projects findProject(UUID id) {
        Optional<Projects> project = projectRepository.findById(id);

        // Báo lỗi khi dự án không tồn tại.
        if (project.isEmpty()) {
            throw new NotFoundException("Project not found");
        }

        return project.get();
    }

    // Chọn truy vấn danh sách dự án phù hợp với bộ lọc và phạm vi người dùng.
    private Page<Projects> findProjects(
            String search,
            String status,
            boolean viewOnlyYourProjects,
            UUID currentUserId,
            Pageable pageable) {
        // Giới hạn kết quả vào các dự án mà người dùng là thành viên khi được yêu cầu.
        if (viewOnlyYourProjects) {
            return projectRepository.searchViewableProjects(
                    search.toLowerCase(Locale.ROOT),
                    status.toLowerCase(Locale.ROOT),
                    currentUserId,
                    pageable
            );
        }

        // Dùng truy vấn mặc định khi không có từ khóa hoặc trạng thái.
        if (search.isBlank() && status.isBlank()) {
            return projectRepository.findAll(pageable);
        }

        // Dùng truy vấn theo trạng thái khi không có từ khóa.
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

    // Tạo cấu hình phân trang và sắp xếp dự án mới nhất trước.
    private Pageable createPageable(int page) {
        Sort newestProjectFirst = Sort.by(
                Sort.Direction.DESC,
                "projectCreatedAt"
        );

        return PageRequest.of(page, PAGE_SIZE, newestProjectFirst);
    }

    // Kiểm tra và áp dụng các trường thông tin cơ bản vào entity dự án.
    private void applyProjectInformation(
            Projects project,
            String projectNameValue,
            String projectCodeValue,
            LocalDate startDate,
            LocalDate endDate,
            String descriptionValue,
            UUID currentProjectId) {
        String projectName = projectNameValue.trim();
        String projectCode = projectCodeValue.trim();
        String description = normalize(descriptionValue);

        validateProjectDateRange(startDate, endDate);

        // Không cho phép tạo dự án mới đã kết thúc trong quá khứ.
        if (currentProjectId == null
                && endDate.isBefore(LocalDate.now(PROJECT_TIME_ZONE))) {
            throw new BadHttpException(
                    "Project end date must not be before today"
            );
        }

        boolean duplicateCode;

        // Chọn cách kiểm tra trùng mã phù hợp cho thao tác tạo hoặc cập nhật.
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

        // Ngăn tạo hoặc cập nhật thành mã dự án đã được sử dụng.
        if (duplicateCode) {
            throw new BadHttpException("Project code already exists");
        }

        project.setProjectName(projectName);
        project.setProjectCode(projectCode);
        project.setProjectStartDate(startDate);
        project.setProjectEndDate(endDate);
        project.setProjectDescription(description);
    }

    // Kiểm tra ngày bắt đầu và kết thúc của dự án hợp lệ.
    private void validateProjectDateRange(
            LocalDate startDate,
            LocalDate endDate) {
        // Ngăn ngày bắt đầu nằm sau ngày kết thúc.
        if (startDate.isAfter(endDate)) {
            throw new BadHttpException(
                    "Start date must not be after end date"
            );
        }
    }

    // Kiểm tra dự án có đang ở trạng thái Completed hay không.
    private boolean isCompletedProject(Projects project) {
        String status = normalize(project.getProjectStatus());
        return COMPLETED_PROJECT_STATUS.equalsIgnoreCase(status);
    }

    // Tạo danh sách thành viên ban đầu và luôn gán toàn quyền cho người tạo dự án.
    private List<ProjectMemberRequest> createInitialMembers(
            List<ProjectMemberRequest> requestedMembers,
            UUID projectCreatorId,
            UUID fullAccessPermissionId) {
        List<ProjectMemberRequest> initialMembers = new ArrayList<>();

        // Sao chép thành viên được chọn nhưng loại người tạo để tránh trùng lặp.
        if (requestedMembers != null) {
            for (ProjectMemberRequest member : requestedMembers) {
                if (projectCreatorId.equals(member.userId())) {
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

    // Chuyển entity dự án thành phần tử danh sách kèm quyền xem và phê duyệt.
    private ProjectListItemResponse toListItem(Projects project,Users currentUser) {
        boolean isProjectMember = projectMemberRepository.countByProjectIdAndUserId(project.getId(),currentUser.getId()) > 0;
        boolean canView = isProjectMember || projectApprovalService.canReviewProjects(currentUser);
        boolean canApprove = projectApprovalService.canApproveProject(project, currentUser);

        return new ProjectListItemResponse(
                project.getId(),
                project.getProjectCode(),
                project.getProjectName(),
                project.getProjectDescription(),
                project.getProjectStatus(),
                project.getProjectStartDate(),
                project.getProjectEndDate(),
                project.getProjectCreatedBy().getFirstName() + " " + project.getProjectCreatedBy().getLastName(),
                project.getProjectCreatedAt(),
                canView,
                canApprove
        );
    }

    // Chuyển entity dự án thành dữ liệu chi tiết theo phạm vi truy cập.
    private ProjectDetailResponse toDetail(Projects project, ProjectAccessResponse access) {
        UUID projectId = project.getId();
        boolean canManageMembers = permissionAccessService.hasAction(access, "MANAGE_MEMBERS");
        boolean canViewMembers = canManageMembers || access.isExecutiveViewer();
        boolean canViewContracts = permissionAccessService.hasAction(access, "VIEW_CONTRACTS");

        List<ProjectPhaseResponse> phases = projectPhaseService.getProjectPhases(projectId);
        List<ProjectUserResponse> users = List.of();
        List<ProjectPermissionOptionResponse> permissionOptions = List.of();
        List<ProjectContractResponse> contracts = List.of();

        // Chỉ tải thành viên khi người dùng được phép xem dữ liệu thành viên.
        if (canViewMembers) {
            users = projectMemberService.getProjectUsers(projectId);
        }

        // Chỉ tải tùy chọn quyền khi người dùng có thể quản lý thành viên.
        if (canManageMembers) {
            permissionOptions = projectPermissionService.getOptions(projectId);
        }

        // Chỉ tải hợp đồng khi người dùng có action xem hợp đồng.
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
                project.getProjectCreatedBy().getFirstName()
                        + " "
                        + project.getProjectCreatedBy().getLastName(),
                project.getProjectCreatedAt(),
                phases,
                users,
                permissionOptions,
                contracts,
                access
        );
    }

    // Kiểm tra request cập nhật có chứa ít nhất một trường thông tin dự án.
    private boolean hasProjectInformation(ProjectUpdateRequest request) {
        return request.projectName() != null
                || request.projectCode() != null
                || request.projectStartDate() != null
                || request.projectEndDate() != null
                || request.projectDescription() != null;
    }

    // Kiểm tra nhóm thông tin bắt buộc được gửi đầy đủ khi cập nhật dự án.
    private boolean hasCompleteProjectInformation(ProjectUpdateRequest request) {
        return request.projectName() != null
                && request.projectCode() != null
                && request.projectStartDate() != null
                && request.projectEndDate() != null;
    }

    // Chuyển danh sách hợp đồng của dự án thành dữ liệu rút gọn cho client.
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

    // Chuẩn hóa chuỗi null thành rỗng và loại bỏ khoảng trắng hai đầu.
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
