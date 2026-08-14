package com.fpt.backend.service.impl.permission;

import com.fpt.backend.dto.request.permission.PermissionListRequest;
import com.fpt.backend.dto.request.permission.PermissionRequest;
import com.fpt.backend.dto.response.permission.PermissionActionResponse;
import com.fpt.backend.dto.response.permission.PermissionDetailResponse;
import com.fpt.backend.dto.response.permission.PermissionListResponse;
import com.fpt.backend.dto.response.permission.PermissionProjectResponse;
import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.permission.PermissionRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.impl.project.ProjectApprovalService;
import com.fpt.backend.service.interfaces.permission.IPermissionAccessService;
import com.fpt.backend.util.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

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

    // Kiểm tra danh sách permission áp dụng đúng bộ lọc, phân trang và đánh dấu dự án có thể quản lý.
    @Test
    void getPermissions_filtersPageAndMarksManageableProjects() {
        Users user = user();
        Projects manageableProject = project("Beta");
        Projects readonlyProject = project("Alpha");
        Permissions manageable = permission(manageableProject, "MANAGER");
        Permissions readonly = permission(readonlyProject, "VIEWER");
        PermissionListRequest request = new PermissionListRequest(
                " Manager ", null, true, 1, "projectName", "asc"
        );
        Pageable repositoryPageable = PageRequest.of(
                1, 7, Sort.by(Sort.Direction.ASC, "project.projectName")
        );
        when(currentUser.getCurrentUser()).thenReturn(user);
        when(projectApprovalService.canReviewProjects(user)).thenReturn(false);
        when(permissionAccessService.getCurrentUserProjectIdsWithAction("MANAGE_MEMBERS"))
                .thenReturn(List.of(manageableProject.getId()));
        when(permissionRepository.searchPermissions(
                "manager", null, true, user.getId(), "MANAGE_MEMBERS", false,
                repositoryPageable
        )).thenReturn(new PageImpl<>(
                List.of(manageable, readonly), repositoryPageable, 9
        ));

        PermissionListResponse result = permissionService.getPermissions(request);

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).isCanManage()).isTrue();
        assertThat(result.getItems().get(1).isCanManage()).isFalse();
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(7);
        assertThat(result.getTotalElements()).isEqualTo(9);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    // Kiểm tra người có action quản lý nhận chi tiết permission với trạng thái có thể chỉnh sửa.
    @Test
    void getPermissionById_manager_returnsManageableDetail() {
        Permissions permission = permission(project("Alpha"), "MANAGER");
        ProjectAccessResponse access = ProjectAccessResponse.builder().build();
        when(permissionRepository.findById(permission.getId()))
                .thenReturn(Optional.of(permission));
        when(permissionAccessService.getCurrentUserAccess(
                permission.getProject().getId()
        )).thenReturn(access);
        when(permissionAccessService.hasAction(access, "MANAGE_MEMBERS"))
                .thenReturn(true);
        when(permissionActionService.getAllowedActionCodes(permission))
                .thenReturn(List.of("VIEW_TASKS"));
        when(permissionActionService.getActionDetails(permission)).thenReturn(List.of());
        when(permissionActionService.getWorkScope(permission)).thenReturn("FULL");

        PermissionDetailResponse result =
                permissionService.getPermissionById(permission.getId());

        assertThat(result.getId()).isEqualTo(permission.getId());
        assertThat(result.isCanManage()).isTrue();
        assertThat(result.getAllowedActions()).containsExactly("VIEW_TASKS");
    }

    // Kiểm tra tạo permission chuẩn hóa chuỗi, cấu hình action và lưu dữ liệu hợp lệ.
    @Test
    void createPermission_validRequest_trimsFieldsConfiguresActionsAndSaves() {
        Projects project = project("Alpha");
        PermissionRequest request = request(project.getId(), " Manager ", " MANAGER ");
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(permissionRepository.existsByPermissionCodeIgnoreCase("MANAGER"))
                .thenReturn(false);
        when(permissionRepository.save(any(Permissions.class))).thenAnswer(invocation -> {
            Permissions saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(permissionActionService.getAllowedActionCodes(any(Permissions.class)))
                .thenReturn(request.allowedActions());
        when(permissionActionService.getActionDetails(any(Permissions.class)))
                .thenReturn(List.of());
        when(permissionActionService.getWorkScope(any(Permissions.class)))
                .thenReturn("FULL");

        PermissionDetailResponse result = permissionService.createPermission(request);

        ArgumentCaptor<Permissions> captor = ArgumentCaptor.forClass(Permissions.class);
        verify(permissionRepository).save(captor.capture());
        assertThat(captor.getValue()).satisfies(permission -> {
            assertThat(permission.getPermissionName()).isEqualTo("Manager");
            assertThat(permission.getPermissionCode()).isEqualTo("MANAGER");
            assertThat(permission.getProject()).isSameAs(project);
            assertThat(permission.getCreatedAt()).isNotNull();
        });
        verify(permissionAccessService).requireAction(project.getId(), "MANAGE_MEMBERS");
        verify(permissionActionService).configurePermission(
                captor.getValue(), request.allowedActions(), request.workScope()
        );
        assertThat(result.getPermissionCode()).isEqualTo("MANAGER");
    }

    // Kiểm tra trùng permission code bị từ chối trước khi lưu hoặc cấu hình action.
    @Test
    void createPermission_duplicateCode_rejectsBeforeSavingOrConfiguringActions() {
        Projects project = project("Alpha");
        PermissionRequest request = request(project.getId(), "Manager", "MANAGER");
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(permissionRepository.existsByPermissionCodeIgnoreCase("MANAGER"))
                .thenReturn(true);

        assertThatThrownBy(() -> permissionService.createPermission(request))
                .isInstanceOf(BadHttpException.class)
                .hasMessage("Permission code already exists");
        verify(permissionRepository, never()).save(any(Permissions.class));
        verify(permissionActionService, never()).configurePermission(
                any(), any(), any()
        );
    }

    // Kiểm tra cập nhật giữ nguyên createdAt và chỉ kiểm tra code trùng với permission khác.
    @Test
    void updatePermission_validRequest_preservesCreatedAtAndChecksCodeAgainstOtherIds() {
        Projects project = project("Alpha");
        Permissions permission = permission(project, "OLD_CODE");
        LocalDateTime createdAt = permission.getCreatedAt();
        PermissionRequest request = request(project.getId(), "Updated", "UPDATED_CODE");
        when(permissionRepository.findById(permission.getId()))
                .thenReturn(Optional.of(permission));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(permissionRepository.existsByPermissionCodeIgnoreCaseAndIdNot(
                "UPDATED_CODE", permission.getId()
        )).thenReturn(false);
        when(permissionRepository.save(permission)).thenReturn(permission);

        PermissionDetailResponse result =
                permissionService.updatePermission(permission.getId(), request);

        assertThat(permission.getCreatedAt()).isEqualTo(createdAt);
        assertThat(permission.getPermissionCode()).isEqualTo("UPDATED_CODE");
        assertThat(result.getId()).isEqualTo(permission.getId());
        verify(permissionAccessService, times(2))
                .requireAction(project.getId(), "MANAGE_MEMBERS");
    }

    // Kiểm tra xóa permission yêu cầu action quản lý đúng dự án trước khi gọi repository delete.
    @Test
    void deletePermission_existingProjectPermission_requiresManageActionAndDeletes() {
        Permissions permission = permission(project("Alpha"), "MANAGER");
        when(permissionRepository.findById(permission.getId()))
                .thenReturn(Optional.of(permission));

        permissionService.deletePermission(permission.getId());

        verify(permissionAccessService).requireAction(
                permission.getProject().getId(), "MANAGE_MEMBERS"
        );
        verify(permissionRepository).delete(permission);
    }

    // Kiểm tra xóa permission không tồn tại trả về lỗi NotFound.
    @Test
    void deletePermission_unknownPermission_throwsNotFound() {
        UUID permissionId = UUID.randomUUID();
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.deletePermission(permissionId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Permission not found");
    }

    private static Users user() {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        return user;
    }

    private static Projects project(String name) {
        Projects project = new Projects();
        project.setId(UUID.randomUUID());
        project.setProjectCode("PRJ-2026-" + name);
        project.setProjectName(name);
        return project;
    }

    private static Permissions permission(Projects project, String code) {
        Permissions permission = new Permissions();
        permission.setId(UUID.randomUUID());
        permission.setPermissionName(code);
        permission.setPermissionCode(code);
        permission.setPermissionDescription("Description");
        permission.setStatus(true);
        permission.setProject(project);
        permission.setCreatedAt(LocalDateTime.of(2026, 8, 1, 8, 0));
        return permission;
    }

    private static PermissionRequest request(
            UUID projectId,
            String name,
            String code) {
        return new PermissionRequest(
                name,
                code,
                " Description ",
                true,
                projectId,
                List.of("VIEW_TASKS"),
                "FULL"
        );
    }
}
