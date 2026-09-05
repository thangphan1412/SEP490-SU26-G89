package com.fpt.backend.controller.roleController;

import com.fpt.backend.dto.request.role.RoleRequestDTO;
import com.fpt.backend.dto.response.role.RoleResponseDTO;
import com.fpt.backend.service.interfaces.role.IRoleService;
import com.fpt.backend.util.BaseResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

        @Mock
        private IRoleService roleService;

        @InjectMocks
        private RoleController roleController;

        /**
         * TC01 - Tìm kiếm Role có dữ liệu phù hợp.
         * Input: search = "admin"; service trả về hai Role ADMIN và SYSTEM_ADMIN.
         * Expected: HTTP 200, message thành công và data giữ nguyên hai Role từ
         * service.
         */
        @Test
        void searchRoles_returnsOkWithMatchingRoles() {
                String search = "admin";
                List<RoleResponseDTO> expectedRoles = List.of(
                                role(
                                                "00000000-0000-0000-0000-000000000001",
                                                "ADMIN",
                                                "Administrator",
                                                "Manage users and roles"),
                                role(
                                                "00000000-0000-0000-0000-000000000002",
                                                "SYSTEM_ADMIN",
                                                "System Administrator",
                                                "Manage system configuration"));
                when(roleService.searchRoles(search)).thenReturn(expectedRoles);

                ResponseEntity<BaseResponse<List<RoleResponseDTO>>> response = roleController.searchRoles(search);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).satisfies(body -> {
                        assertThat(body.getStatus()).isEqualTo(HttpStatus.OK.value());
                        assertThat(body.getMessage()).isEqualTo("Successfully searched roles");
                        assertThat(body.getData()).isSameAs(expectedRoles);
                        assertThat(body.getData()).containsExactlyElementsOf(expectedRoles);
                });
                verify(roleService).searchRoles(search);
        }

        /**
         * TC02 - Search rỗng để tải toàn bộ Role cho màn hình List Role.
         * Input: search = ""; service trả về hai Role CEO và EMPLOYEE.
         * Expected: HTTP 200, message thành công và data chứa toàn bộ hai Role.
         */
        @Test
        void searchRoles_withEmptySearchReturnsAllRoles() {
                String search = "";
                List<RoleResponseDTO> expectedRoles = List.of(
                                role(
                                                "00000000-0000-0000-0000-000000000003",
                                                "CEO",
                                                "Chief Executive Officer",
                                                "Company executive"),
                                role(
                                                "00000000-0000-0000-0000-000000000004",
                                                "EMPLOYEE",
                                                "Employee",
                                                "Company employee"));
                when(roleService.searchRoles(search)).thenReturn(expectedRoles);

                ResponseEntity<BaseResponse<List<RoleResponseDTO>>> response = roleController.searchRoles(search);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).satisfies(body -> {
                        assertThat(body.getStatus()).isEqualTo(HttpStatus.OK.value());
                        assertThat(body.getMessage()).isEqualTo("Successfully searched roles");
                        assertThat(body.getData()).containsExactlyElementsOf(expectedRoles);
                });
                verify(roleService).searchRoles("");
        }

        /**
         * TC03 - Tìm kiếm Role nhưng không có dữ liệu phù hợp.
         * Input: search = "not-found"; service trả về danh sách rỗng.
         * Expected: HTTP 200, message thành công và data là danh sách rỗng, không phải
         * null.
         */
        @Test
        void searchRoles_returnsOkWithEmptyListWhenNoRoleMatches() {
                String search = "not-found";
                when(roleService.searchRoles(search)).thenReturn(List.of());

                ResponseEntity<BaseResponse<List<RoleResponseDTO>>> response = roleController.searchRoles(search);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).satisfies(body -> {
                        assertThat(body.getStatus()).isEqualTo(HttpStatus.OK.value());
                        assertThat(body.getMessage()).isEqualTo("Successfully searched roles");
                        assertThat(body.getData()).isNotNull().isEmpty();
                });
                verify(roleService).searchRoles(search);
        }

        /**
         * TC04 - Service phát sinh lỗi khi tìm kiếm Role.
         * Input: search = "error"; service ném RuntimeException "Unable to search
         * roles".
         * Expected: controller truyền nguyên exception ra ngoài để AppExceptionHandler
         * xử lý.
         */
        @Test
        void searchRoles_propagatesServiceException() {
                String search = "error";
                RuntimeException serviceException = new RuntimeException("Unable to search roles");
                when(roleService.searchRoles(search)).thenThrow(serviceException);

                assertThatThrownBy(() -> roleController.searchRoles(search))
                                .isSameAs(serviceException)
                                .hasMessage("Unable to search roles");
                verify(roleService).searchRoles(search);
        }

        /**
         * TC05 - Tạo Role thành công với đầy đủ thông tin.
         * Input: code = "ADMIN", name = "Administrator", description = "Manage users and roles".
         * Expected: HTTP 201, message tạo thành công và data là Role do service trả về.
         */
        @Test
        void createRole_returnsCreatedWithCompleteRequest() {
                RoleRequestDTO request = new RoleRequestDTO(
                                "ADMIN",
                                "Administrator",
                                "Manage users and roles");
                RoleResponseDTO createdRole = role(
                                "00000000-0000-0000-0000-000000000005",
                                "ADMIN",
                                "Administrator",
                                "Manage users and roles");
                when(roleService.createRole(request)).thenReturn(createdRole);

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.createRole(request);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                assertThat(response.getBody()).satisfies(body -> {
                        assertThat(body.getStatus()).isEqualTo(HttpStatus.CREATED.value());
                        assertThat(body.getMessage()).isEqualTo("Role created successfully");
                        assertThat(body.getData()).isSameAs(createdRole);
                });
                verify(roleService).createRole(request);
        }

        /**
         * TC06 - Tạo Role thành công khi description không được cung cấp.
         * Input: code = "EMPLOYEE", name = "Employee", description = null.
         * Expected: HTTP 201 và data chứa Role EMPLOYEE có description null.
         */
        @Test
        void createRole_returnsCreatedWhenDescriptionIsNull() {
                RoleRequestDTO request = new RoleRequestDTO(
                                "EMPLOYEE",
                                "Employee",
                                null);
                RoleResponseDTO createdRole = role(
                                "00000000-0000-0000-0000-000000000006",
                                "EMPLOYEE",
                                "Employee",
                                null);
                when(roleService.createRole(request)).thenReturn(createdRole);

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.createRole(request);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                assertThat(response.getBody()).satisfies(body -> {
                        assertThat(body.getStatus()).isEqualTo(HttpStatus.CREATED.value());
                        assertThat(body.getMessage()).isEqualTo("Role created successfully");
                        assertThat(body.getData()).isSameAs(createdRole);
                        assertThat(body.getData().getRoleDescription()).isNull();
                });
                verify(roleService).createRole(request);
        }

        /**
         * TC07 - Tạo Role thành công khi service chuẩn hóa role code viết thường.
         * Input: code = "project_manager", name = "Project Manager".
         * Expected: HTTP 201 và data chứa role code đã chuẩn hóa thành "PROJECT_MANAGER".
         */
        @Test
        void createRole_returnsCreatedRoleNormalizedByService() {
                RoleRequestDTO request = new RoleRequestDTO(
                                "project_manager",
                                "Project Manager",
                                "Manage projects");
                RoleResponseDTO createdRole = role(
                                "00000000-0000-0000-0000-000000000007",
                                "PROJECT_MANAGER",
                                "Project Manager",
                                "Manage projects");
                when(roleService.createRole(request)).thenReturn(createdRole);

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.createRole(request);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                assertThat(response.getBody()).satisfies(body -> {
                        assertThat(body.getStatus()).isEqualTo(HttpStatus.CREATED.value());
                        assertThat(body.getMessage()).isEqualTo("Role created successfully");
                        assertThat(body.getData().getRoleCode()).isEqualTo("PROJECT_MANAGER");
                });
                verify(roleService).createRole(request);
        }

        /**
         * TC08 - Tạo Role thất bại do role code đã tồn tại.
         * Input: code = "ADMIN", name = "Another Administrator"; service báo trùng code.
         * Expected: HTTP 400, data null và message "Role code is already in use!".
         */
        @Test
        void createRole_returnsBadRequestWhenRoleCodeAlreadyExists() {
                RoleRequestDTO request = new RoleRequestDTO(
                                "ADMIN",
                                "Another Administrator",
                                "Duplicate role code");
                String errorMessage = "Role code is already in use!";
                when(roleService.createRole(request))
                                .thenThrow(new RuntimeException(errorMessage));

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.createRole(request);

                assertCreateBadRequest(response, errorMessage);
                verify(roleService).createRole(request);
        }

        /**
         * TC09 - Tạo Role thất bại do role code sai định dạng.
         * Input: code = "A-", name = "Invalid Code Role"; service báo code không hợp lệ.
         * Expected: HTTP 400, data null và giữ nguyên validation message từ service.
         */
        @Test
        void createRole_returnsBadRequestWhenRoleCodeIsInvalid() {
                RoleRequestDTO request = new RoleRequestDTO(
                                "A-",
                                "Invalid Code Role",
                                "Invalid role code format");
                String errorMessage =
                                "Role code must contain 2-50 uppercase letters, numbers, or underscores";
                when(roleService.createRole(request))
                                .thenThrow(new RuntimeException(errorMessage));

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.createRole(request);

                assertCreateBadRequest(response, errorMessage);
                verify(roleService).createRole(request);
        }

        /**
         * TC10 - Tạo Role thất bại do role name để trống.
         * Input: code = "MANAGER", name = "   "; service báo role name bắt buộc.
         * Expected: HTTP 400, data null và message "Role name is required".
         */
        @Test
        void createRole_returnsBadRequestWhenRoleNameIsBlank() {
                RoleRequestDTO request = new RoleRequestDTO(
                                "MANAGER",
                                "   ",
                                "Role without a valid name");
                String errorMessage = "Role name is required";
                when(roleService.createRole(request))
                                .thenThrow(new RuntimeException(errorMessage));

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.createRole(request);

                assertCreateBadRequest(response, errorMessage);
                verify(roleService).createRole(request);
        }

        /**
         * TC11 - Tạo Role thất bại do role name dài quá giới hạn.
         * Input: code = "MANAGER", name có 101 ký tự; service báo giới hạn là 100.
         * Expected: HTTP 400, data null và message "Role name cannot exceed 100 characters".
         */
        @Test
        void createRole_returnsBadRequestWhenRoleNameExceedsMaximumLength() {
                RoleRequestDTO request = new RoleRequestDTO(
                                "MANAGER",
                                "N".repeat(101),
                                "Role name is too long");
                String errorMessage = "Role name cannot exceed 100 characters";
                when(roleService.createRole(request))
                                .thenThrow(new RuntimeException(errorMessage));

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.createRole(request);

                assertCreateBadRequest(response, errorMessage);
                verify(roleService).createRole(request);
        }

        /**
         * TC12 - Tạo Role thất bại do description dài quá giới hạn.
         * Input: code = "MANAGER", name = "Manager", description có 256 ký tự.
         * Expected: HTTP 400, data null và message "Role description cannot exceed 255 characters".
         */
        @Test
        void createRole_returnsBadRequestWhenDescriptionExceedsMaximumLength() {
                RoleRequestDTO request = new RoleRequestDTO(
                                "MANAGER",
                                "Manager",
                                "D".repeat(256));
                String errorMessage = "Role description cannot exceed 255 characters";
                when(roleService.createRole(request))
                                .thenThrow(new RuntimeException(errorMessage));

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.createRole(request);

                assertCreateBadRequest(response, errorMessage);
                verify(roleService).createRole(request);
        }

        /**
         * TC13 - Tạo Role thất bại khi request là null.
         * Input: request = null; service báo request bắt buộc.
         * Expected: HTTP 400, data null và message "Role request is required".
         */
        @Test
        void createRole_returnsBadRequestWhenRequestIsNull() {
                String errorMessage = "Role request is required";
                when(roleService.createRole(null))
                                .thenThrow(new RuntimeException(errorMessage));

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.createRole(null);

                assertCreateBadRequest(response, errorMessage);
                verify(roleService).createRole(null);
        }

        /**
         * TC14 - Cập nhật Role thành công với đầy đủ thông tin.
         * Input: id hợp lệ, code = "ADMIN", name = "Senior Administrator".
         * Expected: HTTP 200, message cập nhật thành công và data là Role từ service.
         */
        @Test
        void updateRole_returnsOkWithCompleteRequest() {
                UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000008");
                RoleRequestDTO request = new RoleRequestDTO(
                                "ADMIN",
                                "Senior Administrator",
                                "Manage users, roles and permissions");
                RoleResponseDTO updatedRole = updatedRole(
                                roleId,
                                "ADMIN",
                                "Senior Administrator",
                                "Manage users, roles and permissions");
                when(roleService.updateRole(roleId, request)).thenReturn(updatedRole);

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.updateRole(roleId, request);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).satisfies(body -> {
                        assertThat(body.getStatus()).isEqualTo(HttpStatus.OK.value());
                        assertThat(body.getMessage()).isEqualTo("Role updated successfully");
                        assertThat(body.getData()).isSameAs(updatedRole);
                });
                verify(roleService).updateRole(roleId, request);
        }

        /**
         * TC15 - Cập nhật Role thành công khi description là null.
         * Input: id hợp lệ, code = "EMPLOYEE", name = "Employee", description = null.
         * Expected: HTTP 200 và data có description null.
         */
        @Test
        void updateRole_returnsOkWhenDescriptionIsNull() {
                UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000009");
                RoleRequestDTO request = new RoleRequestDTO("EMPLOYEE", "Employee", null);
                RoleResponseDTO updatedRole = updatedRole(
                                roleId,
                                "EMPLOYEE",
                                "Employee",
                                null);
                when(roleService.updateRole(roleId, request)).thenReturn(updatedRole);

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.updateRole(roleId, request);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).satisfies(body -> {
                        assertThat(body.getStatus()).isEqualTo(HttpStatus.OK.value());
                        assertThat(body.getMessage()).isEqualTo("Role updated successfully");
                        assertThat(body.getData()).isSameAs(updatedRole);
                        assertThat(body.getData().getRoleDescription()).isNull();
                });
                verify(roleService).updateRole(roleId, request);
        }

        /**
         * TC16 - Trả Role đã được service chuẩn hóa code sau khi cập nhật.
         * Input: code = "admin" cùng id Role ADMIN.
         * Expected: HTTP 200 và data trả về có code "ADMIN".
         */
        @Test
        void updateRole_returnsRoleCodeNormalizedByService() {
                UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000010");
                RoleRequestDTO request = new RoleRequestDTO(
                                "admin",
                                "Administrator",
                                "Manage users and roles");
                RoleResponseDTO updatedRole = updatedRole(
                                roleId,
                                "ADMIN",
                                "Administrator",
                                "Manage users and roles");
                when(roleService.updateRole(roleId, request)).thenReturn(updatedRole);

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.updateRole(roleId, request);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).satisfies(body -> {
                        assertThat(body.getStatus()).isEqualTo(HttpStatus.OK.value());
                        assertThat(body.getMessage()).isEqualTo("Role updated successfully");
                        assertThat(body.getData().getRoleCode()).isEqualTo("ADMIN");
                });
                verify(roleService).updateRole(roleId, request);
        }

        /**
         * TC17 - Cập nhật Role không tồn tại.
         * Input: id không tồn tại và request hợp lệ; service báo "Role not found".
         * Expected: HTTP 400, data null và giữ nguyên message lỗi từ service.
         */
        @Test
        void updateRole_returnsBadRequestWhenRoleDoesNotExist() {
                UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000011");
                RoleRequestDTO request = new RoleRequestDTO(
                                "ADMIN",
                                "Administrator",
                                "Manage users and roles");
                String errorMessage = "Role not found with id: " + roleId;
                when(roleService.updateRole(roleId, request))
                                .thenThrow(new RuntimeException(errorMessage));

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.updateRole(roleId, request);

                assertUpdateBadRequest(response, errorMessage);
                verify(roleService).updateRole(roleId, request);
        }

        /**
         * TC18 - Không cho phép thay đổi role code sau khi tạo.
         * Input: cập nhật Role ADMIN nhưng request gửi code = "MANAGER".
         * Expected: HTTP 400 và message "Role code cannot be changed after creation!".
         */
        @Test
        void updateRole_returnsBadRequestWhenRoleCodeIsChanged() {
                UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000012");
                RoleRequestDTO request = new RoleRequestDTO(
                                "MANAGER",
                                "Administrator",
                                "Manage users and roles");
                String errorMessage = "Role code cannot be changed after creation!";
                when(roleService.updateRole(roleId, request))
                                .thenThrow(new RuntimeException(errorMessage));

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.updateRole(roleId, request);

                assertUpdateBadRequest(response, errorMessage);
                verify(roleService).updateRole(roleId, request);
        }

        /**
         * TC19 - Cập nhật thất bại khi code bị một Role khác sử dụng.
         * Input: request code = "ADMIN"; service báo trùng code với Role khác.
         * Expected: HTTP 400 và message "Role code is already in use by another role!".
         */
        @Test
        void updateRole_returnsBadRequestWhenCodeBelongsToAnotherRole() {
                UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000013");
                RoleRequestDTO request = new RoleRequestDTO(
                                "ADMIN",
                                "Administrator",
                                "Manage users and roles");
                String errorMessage = "Role code is already in use by another role!";
                when(roleService.updateRole(roleId, request))
                                .thenThrow(new RuntimeException(errorMessage));

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.updateRole(roleId, request);

                assertUpdateBadRequest(response, errorMessage);
                verify(roleService).updateRole(roleId, request);
        }

        /**
         * TC20 - Cập nhật thất bại do role code sai định dạng.
         * Input: code = "A-".
         * Expected: HTTP 400 và message quy tắc role code từ service.
         */
        @Test
        void updateRole_returnsBadRequestWhenRoleCodeIsInvalid() {
                UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000014");
                RoleRequestDTO request = new RoleRequestDTO("A-", "Administrator", null);
                String errorMessage =
                                "Role code must contain 2-50 uppercase letters, numbers, or underscores";
                when(roleService.updateRole(roleId, request))
                                .thenThrow(new RuntimeException(errorMessage));

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.updateRole(roleId, request);

                assertUpdateBadRequest(response, errorMessage);
                verify(roleService).updateRole(roleId, request);
        }

        /**
         * TC21 - Cập nhật thất bại do role name để trống.
         * Input: code = "ADMIN", name = "   ".
         * Expected: HTTP 400 và message "Role name is required".
         */
        @Test
        void updateRole_returnsBadRequestWhenRoleNameIsBlank() {
                UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000015");
                RoleRequestDTO request = new RoleRequestDTO("ADMIN", "   ", null);
                String errorMessage = "Role name is required";
                when(roleService.updateRole(roleId, request))
                                .thenThrow(new RuntimeException(errorMessage));

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.updateRole(roleId, request);

                assertUpdateBadRequest(response, errorMessage);
                verify(roleService).updateRole(roleId, request);
        }

        /**
         * TC22 - Cập nhật thất bại do role name vượt giới hạn.
         * Input: name gồm 101 ký tự.
         * Expected: HTTP 400 và message "Role name cannot exceed 100 characters".
         */
        @Test
        void updateRole_returnsBadRequestWhenRoleNameExceedsMaximumLength() {
                UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000016");
                RoleRequestDTO request = new RoleRequestDTO(
                                "ADMIN",
                                "N".repeat(101),
                                null);
                String errorMessage = "Role name cannot exceed 100 characters";
                when(roleService.updateRole(roleId, request))
                                .thenThrow(new RuntimeException(errorMessage));

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.updateRole(roleId, request);

                assertUpdateBadRequest(response, errorMessage);
                verify(roleService).updateRole(roleId, request);
        }

        /**
         * TC23 - Cập nhật thất bại do description vượt giới hạn.
         * Input: description gồm 256 ký tự.
         * Expected: HTTP 400 và message "Role description cannot exceed 255 characters".
         */
        @Test
        void updateRole_returnsBadRequestWhenDescriptionExceedsMaximumLength() {
                UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000017");
                RoleRequestDTO request = new RoleRequestDTO(
                                "ADMIN",
                                "Administrator",
                                "D".repeat(256));
                String errorMessage = "Role description cannot exceed 255 characters";
                when(roleService.updateRole(roleId, request))
                                .thenThrow(new RuntimeException(errorMessage));

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.updateRole(roleId, request);

                assertUpdateBadRequest(response, errorMessage);
                verify(roleService).updateRole(roleId, request);
        }

        /**
         * TC24 - Cập nhật thất bại khi request là null.
         * Input: id hợp lệ, request = null.
         * Expected: HTTP 400 và message "Role request is required".
         */
        @Test
        void updateRole_returnsBadRequestWhenRequestIsNull() {
                UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000018");
                String errorMessage = "Role request is required";
                when(roleService.updateRole(roleId, null))
                                .thenThrow(new RuntimeException(errorMessage));

                ResponseEntity<BaseResponse<RoleResponseDTO>> response =
                                roleController.updateRole(roleId, null);

                assertUpdateBadRequest(response, errorMessage);
                verify(roleService).updateRole(roleId, null);
        }

        private static void assertCreateBadRequest(
                        ResponseEntity<BaseResponse<RoleResponseDTO>> response,
                        String expectedMessage) {
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(response.getBody()).satisfies(body -> {
                        assertThat(body.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                        assertThat(body.getMessage()).isEqualTo(expectedMessage);
                        assertThat(body.getData()).isNull();
                });
        }

        private static void assertUpdateBadRequest(
                        ResponseEntity<BaseResponse<RoleResponseDTO>> response,
                        String expectedMessage) {
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(response.getBody()).satisfies(body -> {
                        assertThat(body.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                        assertThat(body.getMessage()).isEqualTo(expectedMessage);
                        assertThat(body.getData()).isNull();
                });
        }

        private static RoleResponseDTO updatedRole(
                        UUID id,
                        String roleCode,
                        String roleName,
                        String roleDescription) {
                return RoleResponseDTO.builder()
                                .id(id)
                                .roleCode(roleCode)
                                .roleName(roleName)
                                .roleDescription(roleDescription)
                                .createdAt(LocalDateTime.of(2026, 8, 28, 9, 30))
                                .updatedAt(LocalDateTime.of(2026, 9, 3, 15, 45))
                                .build();
        }

        private static RoleResponseDTO role(
                        String id,
                        String roleCode,
                        String roleName,
                        String roleDescription) {
                return RoleResponseDTO.builder()
                                .id(UUID.fromString(id))
                                .roleCode(roleCode)
                                .roleName(roleName)
                                .roleDescription(roleDescription)
                                .createdAt(LocalDateTime.of(2026, 8, 28, 9, 30))
                                .updatedAt(null)
                                .build();
        }
}
