package com.fpt.backend.controller.projectController;

import com.fpt.backend.dto.request.project.ProjectCreateRequest;
import com.fpt.backend.dto.request.project.ProjectListRequest;
import com.fpt.backend.dto.request.project.ProjectPermissionConfigurationRequest;
import com.fpt.backend.dto.request.project.ProjectUpdateRequest;
import com.fpt.backend.dto.response.project.ProjectDetailResponse;
import com.fpt.backend.dto.response.project.ProjectEmployeeResponse;
import com.fpt.backend.dto.response.project.ProjectListResponse;
import com.fpt.backend.dto.response.project.ProjectPermissionConfigurationResponse;
import com.fpt.backend.enums.ProjectDeleteResult;
import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.service.interfaces.project.IProjectService;
import com.fpt.backend.util.BaseResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private IProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    // Kiểm tra API danh sách dự án trả về HTTP 200, đúng dữ liệu và không cho phép cache.
    @Test
    void getProjects_validRequest_returnsOkResponseWithoutCaching() {
        ProjectListRequest request = new ProjectListRequest("spring", "Active", false, 0);
        ProjectListResponse projects = ProjectListResponse.builder()
                .items(List.of())
                .totalElements(0)
                .totalPages(0)
                .build();
        when(projectService.getProjects(request)).thenReturn(projects);

        ResponseEntity<BaseResponse<ProjectListResponse>> response =
                projectController.getProjects(request);

        assertOkResponse(response, projects);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
        verify(projectService).getProjects(request);
    }

    // Kiểm tra API chi tiết dự án tồn tại trả về HTTP 200, đúng dự án và không cho phép cache.
    @Test
    void getProjectById_existingProject_returnsOkResponseWithoutCaching() {
        UUID projectId = UUID.randomUUID();
        ProjectDetailResponse project = ProjectDetailResponse.builder()
                .id(projectId)
                .projectCode("PRJ-2026-Winter Collection")
                .build();
        when(projectService.getProjectById(projectId)).thenReturn(project);

        ResponseEntity<BaseResponse<ProjectDetailResponse>> response =
                projectController.getProjectById(projectId);

        assertOkResponse(response, project);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
        verify(projectService).getProjectById(projectId);
    }

    // Kiểm tra API tạo dự án trả HTTP 201 cùng thông tin dự án vừa được tạo.
    @Test
    void createProject_validRequest_returnsCreatedResponse() {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "Winter Collection",
                "PRJ-2026-Winter Collection",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusMonths(1),
                "Description",
                List.of(),
                List.of()
        );
        ProjectDetailResponse created = ProjectDetailResponse.builder()
                .id(UUID.randomUUID())
                .projectCode(request.projectCode())
                .build();
        when(projectService.createProject(request)).thenReturn(created);

        ResponseEntity<BaseResponse<ProjectDetailResponse>> response =
                projectController.createProject(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body.getStatus()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(body.getMessage()).isEqualTo("Created");
            assertThat(body.getData()).isSameAs(created);
        });
        verify(projectService).createProject(request);
    }

    // Kiểm tra API cập nhật dự án trả HTTP 200 cùng dữ liệu dự án sau cập nhật.
    @Test
    void updateProject_validRequest_returnsUpdatedProject() {
        UUID projectId = UUID.randomUUID();
        ProjectUpdateRequest request = new ProjectUpdateRequest(
                null, null, null, null, null, null, List.of()
        );
        ProjectDetailResponse updated = ProjectDetailResponse.builder()
                .id(projectId)
                .build();
        when(projectService.updateProject(projectId, request)).thenReturn(updated);

        ResponseEntity<BaseResponse<ProjectDetailResponse>> response =
                projectController.updateProject(projectId, request);

        assertOkResponse(response, updated);
        verify(projectService).updateProject(projectId, request);
    }

    // Kiểm tra API xóa dự án chưa có hợp đồng trả về thông báo đã xóa vĩnh viễn.
    @Test
    void deleteProject_withoutContracts_returnsPermanentDeletionMessage() {
        UUID projectId = UUID.randomUUID();
        when(projectService.deleteProject(projectId))
                .thenReturn(ProjectDeleteResult.DELETED_PERMANENTLY);

        ResponseEntity<BaseResponse<Void>> response =
                projectController.deleteProject(projectId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body.getMessage()).isEqualTo("Project deleted permanently");
            assertThat(body.getData()).isNull();
        });
    }

    // Kiểm tra API xóa dự án đã có hợp đồng trả về thông báo chuyển trạng thái sang Cancelled.
    @Test
    void deleteProject_withContracts_returnsCancelledStatusMessage() {
        UUID projectId = UUID.randomUUID();
        when(projectService.deleteProject(projectId))
                .thenReturn(ProjectDeleteResult.STATUS_CHANGED_TO_CANCELLED);

        ResponseEntity<BaseResponse<Void>> response =
                projectController.deleteProject(projectId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body.getMessage())
                    .isEqualTo("Project has contracts, so its status was changed to Cancelled");
            assertThat(body.getData()).isNull();
        });
    }

    private static <T> void assertOkResponse(
            ResponseEntity<BaseResponse<T>> response,
            T expectedData) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(body.getMessage()).isEqualTo(HttpStatus.OK.getReasonPhrase());
            assertThat(body.getData()).isSameAs(expectedData);
        });
    }
}
