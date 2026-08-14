package com.fpt.backend.controller.permissionController;

import com.fpt.backend.dto.request.permission.PermissionListRequest;
import com.fpt.backend.dto.request.permission.PermissionRequest;
import com.fpt.backend.dto.response.permission.PermissionActionResponse;
import com.fpt.backend.dto.response.permission.PermissionDetailResponse;
import com.fpt.backend.dto.response.permission.PermissionListResponse;
import com.fpt.backend.dto.response.permission.PermissionProjectResponse;
import com.fpt.backend.service.interfaces.permission.IPermissionService;
import com.fpt.backend.util.BaseResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionControllerTest {

    @Mock
    private IPermissionService permissionService;

    @InjectMocks
    private PermissionController permissionController;

    // Kiểm tra API danh sách permission trả về HTTP 200, đúng trang dữ liệu và không cho phép cache.
    @Test
    void getPermissions_validRequest_returnsPageWithoutCaching() {
        PermissionListRequest request = new PermissionListRequest(
                "manager", null, true, 0, "createdAt", "desc"
        );
        PermissionListResponse result = PermissionListResponse.builder()
                .items(List.of())
                .page(0)
                .size(7)
                .build();
        when(permissionService.getPermissions(request)).thenReturn(result);

        ResponseEntity<BaseResponse<PermissionListResponse>> response =
                permissionController.getPermissions(request);

        assertOkResponse(response, result);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
        verify(permissionService).getPermissions(request);
    }

    // Kiểm tra API chi tiết permission tồn tại trả đúng dữ liệu và không cho phép cache.
    @Test
    void getPermissionById_existingPermission_returnsDetailWithoutCaching() {
        UUID permissionId = UUID.randomUUID();
        PermissionDetailResponse detail = PermissionDetailResponse.builder()
                .id(permissionId)
                .permissionCode("PROJECT_MANAGER")
                .build();
        when(permissionService.getPermissionById(permissionId)).thenReturn(detail);

        ResponseEntity<BaseResponse<PermissionDetailResponse>> response =
                permissionController.getPermissionById(permissionId);

        assertOkResponse(response, detail);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
        verify(permissionService).getPermissionById(permissionId);
    }

    // Kiểm tra API tạo permission trả HTTP 201 cùng permission vừa tạo.
    @Test
    void createPermission_validRequest_returnsCreatedResponse() {
        PermissionRequest request = request();
        PermissionDetailResponse created = PermissionDetailResponse.builder()
                .id(UUID.randomUUID())
                .permissionCode(request.permissionCode())
                .build();
        when(permissionService.createPermission(request)).thenReturn(created);

        ResponseEntity<BaseResponse<PermissionDetailResponse>> response =
                permissionController.createPermission(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body.getStatus()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(body.getMessage()).isEqualTo("Created");
            assertThat(body.getData()).isSameAs(created);
        });
        verify(permissionService).createPermission(request);
    }

    // Kiểm tra API cập nhật permission trả HTTP 200 cùng dữ liệu sau cập nhật.
    @Test
    void updatePermission_validRequest_returnsUpdatedDetail() {
        UUID permissionId = UUID.randomUUID();
        PermissionRequest request = request();
        PermissionDetailResponse updated = PermissionDetailResponse.builder()
                .id(permissionId)
                .build();
        when(permissionService.updatePermission(permissionId, request)).thenReturn(updated);

        ResponseEntity<BaseResponse<PermissionDetailResponse>> response =
                permissionController.updatePermission(permissionId, request);

        assertOkResponse(response, updated);
        verify(permissionService).updatePermission(permissionId, request);
    }

    // Kiểm tra API xóa permission gọi đúng service và trả về thông báo Deleted.
    @Test
    void deletePermission_existingPermission_returnsDeletedMessage() {
        UUID permissionId = UUID.randomUUID();

        ResponseEntity<BaseResponse<Void>> response =
                permissionController.deletePermission(permissionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(body.getMessage()).isEqualTo("Deleted");
            assertThat(body.getData()).isNull();
        });
        verify(permissionService).deletePermission(permissionId);
    }

    private static PermissionRequest request() {
        return new PermissionRequest(
                "Project Manager",
                "PROJECT_MANAGER",
                "Manage project work",
                true,
                UUID.randomUUID(),
                List.of("VIEW_TASKS", "EDIT_TASKS"),
                "FULL"
        );
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
