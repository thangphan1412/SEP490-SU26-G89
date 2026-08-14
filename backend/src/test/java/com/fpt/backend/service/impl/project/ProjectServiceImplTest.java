package com.fpt.backend.service.impl.project;

import com.fpt.backend.dto.request.project.ProjectCreateRequest;
import com.fpt.backend.dto.request.project.ProjectListRequest;
import com.fpt.backend.dto.request.project.ProjectMemberRequest;
import com.fpt.backend.dto.request.project.ProjectPermissionConfigurationRequest;
import com.fpt.backend.dto.request.project.ProjectPhaseRequest;
import com.fpt.backend.dto.request.project.ProjectUpdateRequest;
import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.dto.response.project.ProjectContractResponse;
import com.fpt.backend.dto.response.project.ProjectDetailResponse;
import com.fpt.backend.dto.response.project.ProjectEmployeeResponse;
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
import com.fpt.backend.util.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectContractRepository projectContractRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectCleanupRepository projectCleanupRepository;

    @Mock
    private ProjectPhaseService projectPhaseService;

    @Mock
    private ProjectMemberService projectMemberService;

    @Mock
    private ProjectPermissionService projectPermissionService;

    @Mock
    private ProjectApprovalService projectApprovalService;

    @Mock
    private IPermissionAccessService permissionAccessService;

    @Mock
    private CurrentUser currentUserUtil;

    @InjectMocks
    private ProjectServiceImpl projectService;

    // Kiểm tra lấy dự án không có bộ lọc sẽ dùng trang sắp xếp mới nhất và ánh xạ đúng quyền truy cập.
    @Test
    void getProjects_withoutFilters_usesNewestFirstPageAndMapsAccessFlags() {
        Users currentUser = user("Current", "User");
        Projects project = project("Active");
        Pageable pageable = projectPage(0);
        when(currentUserUtil.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findAll(pageable)).thenReturn(
                new PageImpl<>(List.of(project), pageable, 1)
        );
        when(projectMemberRepository.countByProjectIdAndUserId(
                project.getId(), currentUser.getId()
        )).thenReturn(1L);
        when(projectApprovalService.canApproveProject(project, currentUser)).thenReturn(true);

        ProjectListResponse result = projectService.getProjects(
                new ProjectListRequest(null, null, false, 0)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isEqualTo(project.getId());
            assertThat(item.isCanView()).isTrue();
            assertThat(item.isCanApprove()).isTrue();
            assertThat(item.getProjectCreatedBy()).isEqualTo("Creator User");
        });
    }

    // Kiểm tra người có quyền quản lý và xem hợp đồng nhận đầy đủ các phần dữ liệu chi tiết dự án.
    @Test
    void getProjectById_manageAndContractAccess_loadsAllPermittedDetailSections() {
        Projects project = project("Active");
        ProjectAccessResponse access = access();
        List<ProjectPhaseResponse> phases = List.of(
                ProjectPhaseResponse.builder().id(UUID.randomUUID()).build()
        );
        List<ProjectUserResponse> users = List.of(
                ProjectUserResponse.builder().userId(UUID.randomUUID()).build()
        );
        List<ProjectPermissionOptionResponse> options = List.of(
                ProjectPermissionOptionResponse.builder().id(UUID.randomUUID()).build()
        );
        Contracts contract = new Contracts();
        contract.setId(UUID.randomUUID());
        contract.setContractTitle("Supply");
        contract.setContractNumber("C-001");
        contract.setContractStatus("ACTIVE");
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(permissionAccessService.getCurrentUserAccess(project.getId()))
                .thenReturn(access);
        when(permissionAccessService.hasAction(access, "MANAGE_MEMBERS"))
                .thenReturn(true);
        when(permissionAccessService.hasAction(access, "VIEW_CONTRACTS"))
                .thenReturn(true);
        when(projectPhaseService.getProjectPhases(project.getId())).thenReturn(phases);
        when(projectMemberService.getProjectUsers(project.getId())).thenReturn(users);
        when(projectPermissionService.getOptions(project.getId())).thenReturn(options);
        when(projectContractRepository.findByProjectId(project.getId()))
                .thenReturn(List.of(contract));

        ProjectDetailResponse result = projectService.getProjectById(project.getId());

        assertThat(result.getPhases()).isSameAs(phases);
        assertThat(result.getUsers()).isSameAs(users);
        assertThat(result.getAvailablePermissions()).isSameAs(options);
        assertThat(result.getContracts()).singleElement().satisfies(response -> {
            assertThat(response.getId()).isEqualTo(contract.getId());
            assertThat(response.getContractNumber()).isEqualTo("C-001");
        });
        assertThat(result.getCurrentUserAccess()).isSameAs(access);
    }

    // Kiểm tra yêu cầu chi tiết dự án không tồn tại trả về lỗi NotFound.
    @Test
    void getProjectById_unknownProject_throwsNotFound() {
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(projectId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Project not found");
    }

    // Kiểm tra tạo dự án hợp lệ điều phối đầy đủ proposal, permission, phase và thành viên.
    @Test
    void createProject_validRequest_orchestratesApprovalPermissionPhasesAndMembers() {
        Users creator = user("Lan", "Nguyen");
        Users selectedMember = user("Mai", "Tran");
        UUID fullAccessPermissionId = UUID.randomUUID();
        ProjectCreateRequest request = createRequest(List.of(
                new ProjectMemberRequest(creator.getId(), UUID.randomUUID()),
                new ProjectMemberRequest(selectedMember.getId(), UUID.randomUUID())
        ));
        ProjectAccessResponse access = access();
        when(currentUserUtil.getCurrentUser()).thenReturn(creator);
        when(projectRepository.existsByProjectCodeIgnoreCase(request.projectCode()))
                .thenReturn(false);
        when(projectRepository.save(any(Projects.class))).thenAnswer(invocation -> {
            Projects saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(projectPermissionService.createProjectFullAccessPermission(any(Projects.class)))
                .thenReturn(fullAccessPermissionId);
        when(permissionAccessService.getCurrentUserAccess(any(UUID.class)))
                .thenReturn(access);
        when(permissionAccessService.hasAction(access, "MANAGE_MEMBERS"))
                .thenReturn(false);
        when(permissionAccessService.hasAction(access, "VIEW_CONTRACTS"))
                .thenReturn(false);
        when(projectPhaseService.getProjectPhases(any(UUID.class))).thenReturn(List.of());

        ProjectDetailResponse result = projectService.createProject(request);

        ArgumentCaptor<Projects> projectCaptor = ArgumentCaptor.forClass(Projects.class);
        verify(projectRepository).save(projectCaptor.capture());
        Projects savedProject = projectCaptor.getValue();
        assertThat(savedProject.getProjectName()).isEqualTo("Winter Collection");
        assertThat(savedProject.getProjectCode())
                .isEqualTo("PRJ-2026-Winter Collection");
        assertThat(savedProject.getProjectStatus()).isEqualTo("On Hold");
        assertThat(savedProject.getProjectCreatedBy()).isSameAs(creator);
        verify(projectApprovalService).createApprovalRequest(savedProject, creator);
        verify(projectPermissionService).createProjectFullAccessPermission(savedProject);
        verify(projectPhaseService).syncPhases(savedProject, request.phases());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProjectMemberRequest>> membersCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(projectMemberService).syncMembers(
                org.mockito.Mockito.eq(savedProject), membersCaptor.capture(),
                org.mockito.Mockito.eq(false)
        );
        assertThat(membersCaptor.getValue()).containsExactly(
                request.members().get(1),
                new ProjectMemberRequest(creator.getId(), fullAccessPermissionId)
        );
        assertThat(result.getId()).isEqualTo(savedProject.getId());
        verify(projectRepository).flush();
    }

    // Kiểm tra từ chối tạo dự án khi project code đã tồn tại trước khi thực hiện lưu.
    @Test
    void createProject_duplicateCode_rejectsBeforeSaving() {
        Users creator = user("Lan", "Nguyen");
        ProjectCreateRequest request = createRequest(List.of());
        when(currentUserUtil.getCurrentUser()).thenReturn(creator);
        when(projectRepository.existsByProjectCodeIgnoreCase(request.projectCode()))
                .thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(BadHttpException.class)
                .hasMessage("Project code already exists");
        verify(projectRepository, never()).save(any(Projects.class));
    }

    // Kiểm tra cập nhật hợp lệ đồng bộ thông tin, phase và thành viên theo đúng quyền được cấp.
    @Test
    void updateProject_informationPhasesAndMembers_updatesAllAuthorizedSections() {
        Projects project = project("Active");
        ProjectAccessResponse access = access();
        LocalDate newEndDate = project.getProjectEndDate().plusDays(10);
        List<ProjectPhaseRequest> phases = List.of(
                new ProjectPhaseRequest(UUID.randomUUID(), "Updated phase", null, newEndDate)
        );
        List<ProjectMemberRequest> members = List.of(
                new ProjectMemberRequest(UUID.randomUUID(), null)
        );
        ProjectUpdateRequest request = new ProjectUpdateRequest(
                " Updated project ",
                "PRJ-2026-Updated Project",
                project.getProjectStartDate(),
                newEndDate,
                " Updated description ",
                phases,
                members
        );
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(permissionAccessService.getCurrentUserAccess(project.getId()))
                .thenReturn(access);
        when(projectRepository.existsByProjectCodeIgnoreCaseAndIdNot(
                request.projectCode(), project.getId()
        )).thenReturn(false);
        when(permissionAccessService.hasAction(access, "MANAGE_MEMBERS"))
                .thenReturn(false);
        when(permissionAccessService.hasAction(access, "VIEW_CONTRACTS"))
                .thenReturn(false);
        when(projectPhaseService.getProjectPhases(project.getId())).thenReturn(List.of());

        ProjectDetailResponse result =
                projectService.updateProject(project.getId(), request);

        assertThat(project.getProjectName()).isEqualTo("Updated project");
        assertThat(project.getProjectDescription()).isEqualTo("Updated description");
        assertThat(project.getProjectEndDate()).isEqualTo(newEndDate);
        verify(permissionAccessService).requireAction(project.getId(), "EDIT_PROJECT");
        verify(permissionAccessService).requireAction(project.getId(), "MANAGE_MEMBERS");
        verify(projectPhaseService).syncPhases(project, phases);
        verify(projectMemberService).syncMembers(project, members, true);
        verify(projectRepository).save(project);
        verify(projectRepository).flush();
        assertThat(result.getProjectName()).isEqualTo("Updated project");
    }

    // Kiểm tra dự án có hợp đồng chỉ chuyển sang Cancelled và không bị xóa dữ liệu.
    @Test
    void deleteProject_existingContracts_changesStatusToCancelledWithoutDeletingData() {
        Projects project = project("Active");
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectContractRepository.countByProjectId(project.getId())).thenReturn(1L);

        ProjectDeleteResult result = projectService.deleteProject(project.getId());

        assertThat(result).isEqualTo(ProjectDeleteResult.STATUS_CHANGED_TO_CANCELLED);
        assertThat(project.getProjectStatus()).isEqualTo("Cancelled");
        verify(projectRepository).save(project);
        verify(projectRepository).flush();
        verify(projectPhaseService, never()).deleteProjectData(project.getId());
    }

    // Kiểm tra dự án không có hợp đồng được xóa cùng dữ liệu phụ thuộc theo đúng thứ tự.
    @Test
    void deleteProject_withoutContracts_deletesDependentDataBeforeProject() {
        Projects project = project("Active");
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectContractRepository.countByProjectId(project.getId())).thenReturn(0L);

        ProjectDeleteResult result = projectService.deleteProject(project.getId());

        assertThat(result).isEqualTo(ProjectDeleteResult.DELETED_PERMANENTLY);
        InOrder order = inOrder(
                projectPhaseService,
                projectMemberService,
                projectPermissionService,
                projectCleanupRepository,
                projectRepository
        );
        order.verify(projectPhaseService).deleteProjectData(project.getId());
        order.verify(projectMemberService).deleteProjectData(project.getId());
        order.verify(projectPermissionService).deleteProjectData(project.getId());
        order.verify(projectCleanupRepository).deleteProjectRecords(project.getId());
        order.verify(projectRepository).delete(project);
        order.verify(projectRepository).flush();
    }

    private static Pageable projectPage(int page) {
        return PageRequest.of(
                page,
                7,
                Sort.by(Sort.Direction.DESC, "projectCreatedAt")
        );
    }

    private static ProjectAccessResponse access() {
        return ProjectAccessResponse.builder()
                .projectId(UUID.randomUUID())
                .currentUserId(UUID.randomUUID())
                .isExecutiveViewer(false)
                .allowedActions(List.of())
                .fullScopeActions(List.of())
                .workScope("OWN")
                .build();
    }

    private static Users user(String firstName, String lastName) {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(firstName.toLowerCase() + "@g89.vn");
        return user;
    }

    private static Projects project(String status) {
        Projects project = new Projects();
        project.setId(UUID.randomUUID());
        project.setProjectCode("PRJ-2026-Winter Collection");
        project.setProjectName("Winter Collection");
        project.setProjectDescription("Description");
        project.setProjectStatus(status);
        project.setProjectStartDate(LocalDate.now().plusDays(1));
        project.setProjectEndDate(LocalDate.now().plusMonths(1));
        project.setProjectCreatedBy(user("Creator", "User"));
        project.setProjectCreatedAt("2026-08-14");
        return project;
    }

    private static ProjectCreateRequest createRequest(
            List<ProjectMemberRequest> members) {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusMonths(1);
        return new ProjectCreateRequest(
                " Winter Collection ",
                "PRJ-2026-Winter Collection",
                start,
                end,
                " Description ",
                List.of(new ProjectPhaseRequest(null, "Planning", null, end)),
                members
        );
    }
}
