package com.fpt.backend.service.impl.permission;

import com.fpt.backend.dto.request.permission.PermissionRequest;
import com.fpt.backend.dto.response.permission.PermissionDetailResponse;
import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.permission.PermissionRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.impl.project.ProjectApprovalService;
import com.fpt.backend.service.interfaces.permission.IPermissionAccessService;
import com.fpt.backend.util.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplCleanupTest {
    private static final UUID PROJECT_ID = new UUID(0, 200);
    private static final UUID PERMISSION_ID = new UUID(0, 201);
    private static final UUID USER_ID = new UUID(0, 202);

    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private PermissionActionService permissionActionService;
    @Mock
    private IPermissionAccessService permissionAccessService;
    @Mock
    private ProjectApprovalService projectApprovalService;
    @Mock
    private CurrentUser currentUser;
    @InjectMocks
    private PermissionServiceImpl permissionService;

    private Projects project;
    private Permissions permission;

    @BeforeEach
    void setUp() {
        project = new Projects();
        project.setId(PROJECT_ID);
        project.setProjectCode("PRJ-2026-Cleanup");
        project.setProjectName("Cleanup Project");
        permission = new Permissions();
        permission.setId(PERMISSION_ID);
        permission.setPermissionName("Test Access");
        permission.setPermissionCode("TEST_ACCESS");
        permission.setProject(project);
    }

    /**
     * Input: quyền 201 thuộc project 200, user có MANAGE_MEMBERS.
     * Expected: xem chi tiết trả đúng id/code/name project và canManage = true.
     */
    @Test
    void getPermissionById_returnsLinkedProject() {
        ProjectAccessResponse access = memberAccess();
        when(permissionRepository.findById(PERMISSION_ID)).thenReturn(Optional.of(permission));
        when(permissionAccessService.getCurrentUserAccess(PROJECT_ID)).thenReturn(access);
        when(permissionAccessService.hasAction(access, "MANAGE_MEMBERS")).thenReturn(true);

        PermissionDetailResponse response = permissionService.getPermissionById(PERMISSION_ID);

        assertProject(response, project);
        assertThat(response.canManage()).isTrue();
    }

    /**
     * Input: quyền 201 không liên kết project.
     * Expected: BadHttpException trước khi ánh xạ DTO; kiểm tra bảo vệ vẫn còn.
     */
    @Test
    void getPermissionById_rejectsPermissionWithoutProject() {
        permission.setProject(null);
        when(permissionRepository.findById(PERMISSION_ID)).thenReturn(Optional.of(permission));

        assertThatThrownBy(() -> permissionService.getPermissionById(PERMISSION_ID))
                .isInstanceOf(BadHttpException.class)
                .hasMessage("Permission is not connected to a project");
        verifyNoInteractions(permissionAccessService);
    }

    /**
     * Input: quyền có project, user không có MANAGE_MEMBERS và không phải điều hành.
     * Expected: HTTP 403; việc dọn code không bỏ kiểm tra quyền xem.
     */
    @Test
    void getPermissionById_preservesAccessCheck() {
        ProjectAccessResponse access = memberAccess();
        when(permissionRepository.findById(PERMISSION_ID)).thenReturn(Optional.of(permission));
        when(permissionAccessService.getCurrentUserAccess(PROJECT_ID)).thenReturn(access);

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> permissionService.getPermissionById(PERMISSION_ID))
                .satisfies(error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    /**
     * Input: tạo TEST_ACCESS cho project 200, action VIEW_TASKS, scope FULL.
     * Expected: DTO trả đúng project đã được kiểm tra và gán trước khi lưu.
     */
    @Test
    void createPermission_returnsValidatedProject() {
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        when(permissionRepository.save(any(Permissions.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PermissionDetailResponse response = permissionService.createPermission(request(PROJECT_ID));

        assertProject(response, project);
        assertThat(response.canManage()).isTrue();
        verify(permissionAccessService).requireAction(PROJECT_ID, "MANAGE_MEMBERS");
    }

    /**
     * Input: cập nhật quyền 201 từ project 200 sang project 203.
     * Expected: DTO trả project 203; kiểm tra MANAGE_MEMBERS ở cả hai project.
     */
    @Test
    void updatePermission_returnsNewValidatedProject() {
        Projects newProject = new Projects();
        newProject.setId(new UUID(0, 203));
        newProject.setProjectCode("PRJ-2026-New");
        newProject.setProjectName("New Project");
        when(permissionRepository.findById(PERMISSION_ID)).thenReturn(Optional.of(permission));
        when(projectRepository.findById(newProject.getId())).thenReturn(Optional.of(newProject));
        when(permissionRepository.save(permission)).thenReturn(permission);

        PermissionDetailResponse response = permissionService.updatePermission(
                PERMISSION_ID, request(newProject.getId()));

        assertProject(response, newProject);
        verify(permissionAccessService).requireAction(PROJECT_ID, "MANAGE_MEMBERS");
        verify(permissionAccessService).requireAction(newProject.getId(), "MANAGE_MEMBERS");
    }

    /**
     * Input: cập nhật quyền 201 đang không liên kết project.
     * Expected: BadHttpException trước khi xử lý request hoặc ánh xạ DTO.
     */
    @Test
    void updatePermission_rejectsPermissionWithoutProject() {
        permission.setProject(null);
        when(permissionRepository.findById(PERMISSION_ID)).thenReturn(Optional.of(permission));

        assertThatThrownBy(() -> permissionService.updatePermission(PERMISSION_ID, request(PROJECT_ID)))
                .isInstanceOf(BadHttpException.class)
                .hasMessage("Permission is not connected to a project");
        verifyNoInteractions(projectRepository, permissionAccessService);
    }

    private PermissionRequest request(UUID projectId) {
        return new PermissionRequest("Test Access", "TEST_ACCESS", "Cleanup regression",
                true, projectId, List.of("VIEW_TASKS"), "FULL");
    }

    private ProjectAccessResponse memberAccess() {
        return new ProjectAccessResponse(PROJECT_ID, USER_ID, false, true, false,
                List.of(), List.of(), "OWN");
    }

    private void assertProject(PermissionDetailResponse response, Projects expectedProject) {
        assertThat(response.projectId()).isEqualTo(expectedProject.getId());
        assertThat(response.projectCode()).isEqualTo(expectedProject.getProjectCode());
        assertThat(response.projectName()).isEqualTo(expectedProject.getProjectName());
    }
}
