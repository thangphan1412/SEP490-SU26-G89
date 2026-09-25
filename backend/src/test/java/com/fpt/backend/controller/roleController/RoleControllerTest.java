package com.fpt.backend.controller.roleController;

import com.fpt.backend.dto.request.role.RoleRequestDTO;
import com.fpt.backend.dto.response.role.RoleResponseDTO;
import com.fpt.backend.entity.Role;
import com.fpt.backend.repository.role.RoleRepository;
import com.fpt.backend.service.impl.role.RoleServiceImpl;
import com.fpt.backend.util.BaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Test gộp RoleController + RoleServiceImpl thật; chỉ mock RoleRepository.
 * Mỗi test gọi controller và kiểm tra cả response lẫn logic nghiệp vụ của service.
 * Không khởi động Spring, HTTP server, Spring Security hoặc database.
 * DisplayName dùng tên chức năng và UTCID để đối chiếu với báo cáo Role Management.
 */
@Tag("role-management")
@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    private static final UUID ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private RoleController roleController;

    @BeforeEach
    void setUp() {
        roleController = new RoleController();
        ReflectionTestUtils.setField(roleController, "roleService", roleService);
    }

    /**
     * Tìm kiếm bằng từ khóa viết hoa toàn bộ.
     * Input: search = "ADMIN"; repository được cấu hình nhận "admin".
     * Expected: HTTP 200, body.status và message thành công;
     * service chuyển input thành chữ thường và trả một DTO ADMIN.
     */
    // List Role - UTCID01
    @Test
    @DisplayName("List Role - UTCID01")
    void searchRoles_withUppercaseKeywordConvertsToLowercase() {
        Role admin = role(
                "00000000-0000-0000-0000-000000000001",
                "ADMIN",
                "Administrator",
                "Manage users and roles",
                LocalDateTime.of(2026, 8, 20, 9, 15),
                null
        );
        when(roleRepository.searchRoles("admin")).thenReturn(List.of(admin));

        List<RoleResponseDTO> result = assertResponse(
                roleController.searchRoles("ADMIN"),
                HttpStatus.OK,
                "Successfully searched roles"
        );
        assertThat(result).isNotNull();

        assertThat(result).singleElement().satisfies(dto ->
                assertThat(dto.getRoleCode()).isEqualTo("ADMIN")
        );
        verify(roleRepository).searchRoles("admin");
    }

    /**
     * Tìm kiếm bằng từ khóa đã viết thường.
     * Input: search = "admin"; repository được gọi với chính từ khóa "admin".
     * Expected: HTTP 200, body.status và message thành công;
     * data trả một DTO ADMIN mà không làm thay đổi từ khóa hợp lệ.
     */
    // List Role - UTCID02
    @Test
    @DisplayName("List Role - UTCID02")
    void searchRoles_withLowercaseKeywordKeepsLowercase() {
        Role admin = role(
                "00000000-0000-0000-0000-000000000002",
                "ADMIN",
                "Administrator",
                "Manage users and roles",
                LocalDateTime.of(2026, 8, 20, 9, 15),
                null
        );
        when(roleRepository.searchRoles("admin")).thenReturn(List.of(admin));

        List<RoleResponseDTO> result = assertResponse(
                roleController.searchRoles("admin"),
                HttpStatus.OK,
                "Successfully searched roles"
        );
        assertThat(result).isNotNull();

        assertThat(result).singleElement().satisfies(dto ->
                assertThat(dto.getRoleName()).isEqualTo("Administrator")
        );
        verify(roleRepository).searchRoles("admin");
    }

    /**
     * Tìm kiếm bằng từ khóa có chữ hoa và chữ thường xen kẽ.
     * Input: search = "AdMiN"; repository được cấu hình nhận "admin".
     * Expected: HTTP 200, body.status và message thành công;
     * service chuyển toàn bộ từ khóa thành chữ thường và trả DTO ADMIN.
     */
    // List Role - UTCID03
    @Test
    @DisplayName("List Role - UTCID03")
    void searchRoles_withMixedCaseKeywordConvertsToLowercase() {
        Role admin = role(
                "00000000-0000-0000-0000-000000000003",
                "ADMIN",
                "Administrator",
                "Manage users and roles",
                LocalDateTime.of(2026, 8, 20, 9, 15),
                null
        );
        when(roleRepository.searchRoles("admin")).thenReturn(List.of(admin));

        List<RoleResponseDTO> result = assertResponse(
                roleController.searchRoles("AdMiN"),
                HttpStatus.OK,
                "Successfully searched roles"
        );
        assertThat(result).isNotNull();

        assertThat(result).singleElement().satisfies(dto ->
                assertThat(dto.getRoleCode()).isEqualTo("ADMIN")
        );
        verify(roleRepository).searchRoles("admin");
    }

    /**
     * Tìm kiếm bằng từ khóa có khoảng trắng ở đầu và cuối.
     * Input: search = "  admin  "; repository được cấu hình nhận "admin".
     * Expected: HTTP 200, body.status và message thành công;
     * service loại bỏ khoảng trắng và trả một DTO ADMIN.
     */
    // List Role - UTCID04
    @Test
    @DisplayName("List Role - UTCID04")
    void searchRoles_trimsLeadingAndTrailingSpaces() {
        Role admin = role(
                "00000000-0000-0000-0000-000000000004",
                "ADMIN",
                "Administrator",
                "Manage users and roles",
                LocalDateTime.of(2026, 8, 20, 9, 15),
                null
        );
        when(roleRepository.searchRoles("admin")).thenReturn(List.of(admin));

        List<RoleResponseDTO> result = assertResponse(
                roleController.searchRoles("  admin  "),
                HttpStatus.OK,
                "Successfully searched roles"
        );
        assertThat(result).isNotNull();

        assertThat(result).singleElement().satisfies(dto ->
                assertThat(dto.getRoleCode()).isEqualTo("ADMIN")
        );
        verify(roleRepository).searchRoles("admin");
    }

    /**
     * Chuyển đầy đủ dữ liệu Role entity sang RoleResponseDTO.
     * Input: search = "admin"; repository trả Role có đủ id, code, name, description và ngày.
     * Expected: HTTP 200, body.status và message thành công;
     * mọi trường dữ liệu trong DTO giống với Role entity ban đầu.
     */
    // List Role - UTCID05
    @Test
    @DisplayName("List Role - UTCID05")
    void searchRoles_mapsEveryEntityFieldToDto() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 20, 9, 15);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 25, 14, 30);
        Role admin = role(
                "00000000-0000-0000-0000-000000000005",
                "ADMIN",
                "Administrator",
                "Manage users and roles",
                createdAt,
                updatedAt
        );
        when(roleRepository.searchRoles("admin")).thenReturn(List.of(admin));

        List<RoleResponseDTO> result = assertResponse(
                roleController.searchRoles("admin"),
                HttpStatus.OK,
                "Successfully searched roles"
        );
        assertThat(result).isNotNull();

        assertThat(result).singleElement().satisfies(dto -> {
            assertThat(dto.getId()).isEqualTo(admin.getId());
            assertThat(dto.getRoleCode()).isEqualTo("ADMIN");
            assertThat(dto.getRoleName()).isEqualTo("Administrator");
            assertThat(dto.getRoleDescription()).isEqualTo("Manage users and roles");
            assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
            assertThat(dto.getUpdatedAt()).isEqualTo(updatedAt);
        });
        verify(roleRepository).searchRoles("admin");
    }

    /**
     * Giữ nguyên thứ tự Role do repository trả về.
     * Input: search = "admin"; repository trả SYSTEM_ADMIN trước ADMIN.
     * Expected: HTTP 200, body.status và message thành công;
     * danh sách DTO cũng có thứ tự SYSTEM_ADMIN rồi ADMIN.
     */
    // List Role - UTCID06
    @Test
    @DisplayName("List Role - UTCID06")
    void searchRoles_preservesRepositoryResultOrder() {
        Role systemAdmin = role(
                "00000000-0000-0000-0000-000000000006",
                "SYSTEM_ADMIN",
                "System Administrator",
                "Manage system configuration",
                LocalDateTime.of(2026, 8, 21, 8, 0),
                null
        );
        Role admin = role(
                "00000000-0000-0000-0000-000000000007",
                "ADMIN",
                "Administrator",
                "Manage users and roles",
                LocalDateTime.of(2026, 8, 20, 9, 15),
                null
        );
        when(roleRepository.searchRoles("admin"))
                .thenReturn(List.of(systemAdmin, admin));

        List<RoleResponseDTO> result = assertResponse(
                roleController.searchRoles("admin"),
                HttpStatus.OK,
                "Successfully searched roles"
        );
        assertThat(result).isNotNull();

        assertThat(result)
                .extracting(RoleResponseDTO::getRoleCode)
                .containsExactly("SYSTEM_ADMIN", "ADMIN");
        verify(roleRepository).searchRoles("admin");
    }

    /**
     * Search bằng chuỗi rỗng.
     * Input: search = ""; repository nhận "" và trả Role CEO, EMPLOYEE.
     * Expected: HTTP 200, body.status và message thành công;
     * data trả hai DTO CEO và EMPLOYEE.
     */
    // List Role - UTCID07
    @Test
    @DisplayName("List Role - UTCID07")
    void searchRoles_withEmptySearchUsesEmptyKeyword() {
        Role ceo = role(
                "00000000-0000-0000-0000-000000000008",
                "CEO",
                "Chief Executive Officer",
                "Company executive",
                LocalDateTime.of(2026, 8, 18, 10, 0),
                null
        );
        Role employee = role(
                "00000000-0000-0000-0000-000000000009",
                "EMPLOYEE",
                "Employee",
                "Company employee",
                LocalDateTime.of(2026, 8, 17, 10, 0),
                null
        );
        when(roleRepository.searchRoles(""))
                .thenReturn(List.of(ceo, employee));

        List<RoleResponseDTO> result = assertResponse(
                roleController.searchRoles(""),
                HttpStatus.OK,
                "Successfully searched roles"
        );
        assertThat(result).isNotNull();

        assertThat(result)
                .extracting(RoleResponseDTO::getRoleCode)
                .containsExactly("CEO", "EMPLOYEE");
        verify(roleRepository).searchRoles("");
    }

    /**
     * Search chỉ chứa khoảng trắng.
     * Input: search = "   "; repository được cấu hình nhận chuỗi rỗng "".
     * Expected: HTTP 200, body.status và message thành công;
     * service loại bỏ khoảng trắng và trả một DTO CEO.
     */
    // List Role - UTCID08
    @Test
    @DisplayName("List Role - UTCID08")
    void searchRoles_withWhitespaceSearchUsesEmptyKeyword() {
        Role ceo = role(
                "00000000-0000-0000-0000-000000000010",
                "CEO",
                "Chief Executive Officer",
                "Company executive",
                LocalDateTime.of(2026, 8, 18, 10, 0),
                null
        );
        when(roleRepository.searchRoles("")).thenReturn(List.of(ceo));

        List<RoleResponseDTO> result = assertResponse(
                roleController.searchRoles("   "),
                HttpStatus.OK,
                "Successfully searched roles"
        );
        assertThat(result).isNotNull();

        assertThat(result).singleElement().satisfies(dto ->
                assertThat(dto.getRoleCode()).isEqualTo("CEO")
        );
        verify(roleRepository).searchRoles("");
    }

    /**
     * Search có giá trị null.
     * Input: search = null; repository được cấu hình nhận chuỗi rỗng "".
     * Expected: HTTP 200, body.status và message thành công;
     * service không phát sinh lỗi và trả một DTO EMPLOYEE.
     */
    // List Role - UTCID09
    @Test
    @DisplayName("List Role - UTCID09")
    void searchRoles_withNullSearchUsesEmptyKeyword() {
        Role employee = role(
                "00000000-0000-0000-0000-000000000011",
                "EMPLOYEE",
                "Employee",
                null,
                LocalDateTime.of(2026, 8, 17, 10, 0),
                null
        );
        when(roleRepository.searchRoles(""))
                .thenReturn(List.of(employee));

        List<RoleResponseDTO> result = assertResponse(
                roleController.searchRoles(null),
                HttpStatus.OK,
                "Successfully searched roles"
        );
        assertThat(result).isNotNull();

        assertThat(result).singleElement().satisfies(dto -> {
            assertThat(dto.getRoleCode()).isEqualTo("EMPLOYEE");
            assertThat(dto.getRoleDescription()).isNull();
        });
        verify(roleRepository).searchRoles("");
    }

    /**
     * Không có Role phù hợp với từ khóa tìm kiếm.
     * Input: search = "not-found"; repository trả về danh sách rỗng.
     * Expected: HTTP 200, body.status và message thành công;
     * data trả danh sách rỗng, không trả null.
     */
    // List Role - UTCID10
    @Test
    @DisplayName("List Role - UTCID10")
    void searchRoles_returnsEmptyListWhenNoRoleMatches() {
        String search = "not-found";
        when(roleRepository.searchRoles(search)).thenReturn(List.of());

        List<RoleResponseDTO> result = assertResponse(
                roleController.searchRoles(search),
                HttpStatus.OK,
                "Successfully searched roles"
        );
        assertThat(result).isNotNull();

        assertThat(result).isNotNull().isEmpty();
        verify(roleRepository).searchRoles(search);
    }

    /**
     * Lấy toàn bộ Role qua hàm getAllRoles.
     * Input: không có tham số; repository được gọi với search = "" và trả hai Role.
     * Expected: HTTP 200, body.status và message thành công;
     * data trả đúng hai DTO ADMIN và EMPLOYEE.
     */
    // Get All Role - UTCID01
    @Test
    @DisplayName("Get All Role - UTCID01")
    void getAllRoles_usesEmptySearchAndMapsAllRoles() {
        Role admin = role(
                "00000000-0000-0000-0000-000000000012",
                "ADMIN",
                "Administrator",
                "Manage users and roles",
                LocalDateTime.of(2026, 8, 20, 9, 15),
                null
        );
        Role employee = role(
                "00000000-0000-0000-0000-000000000013",
                "EMPLOYEE",
                "Employee",
                "Company employee",
                LocalDateTime.of(2026, 8, 17, 10, 0),
                null
        );
        when(roleRepository.searchRoles(""))
                .thenReturn(List.of(admin, employee));

        List<RoleResponseDTO> result = assertResponse(
                roleController.getAllRoles(),
                HttpStatus.OK,
                "Successfully fetched all roles"
        );
        assertThat(result).isNotNull();

        assertThat(result)
                .extracting(RoleResponseDTO::getRoleCode)
                .containsExactly("ADMIN", "EMPLOYEE");
        verify(roleRepository).searchRoles("");
    }

    /**
     * Repository phát sinh lỗi khi tìm kiếm Role.
     * Input: search = "admin"; repository ném RuntimeException "Database unavailable".
     * Expected: controller truyền nguyên RuntimeException và message từ repository.
     */
    // List Role - UTCID11
    @Test
    @DisplayName("List Role - UTCID11")
    void searchRoles_propagatesRepositoryException() {
        String search = "admin";
        RuntimeException repositoryException =
                new RuntimeException("Database unavailable");
        when(roleRepository.searchRoles(search)).thenThrow(repositoryException);

        assertThatThrownBy(() -> roleController.searchRoles(search))
                .isSameAs(repositoryException)
                .hasMessage("Database unavailable");
        verify(roleRepository).searchRoles(search);
    }

    /**
     * Tạo Role thành công với đầy đủ dữ liệu hợp lệ.
     * Input: code = "ADMIN", name = "Administrator", description = "Manage users and roles".
     * Expected: HTTP 201, body.status và message thành công;
     * lưu đúng entity, thiết lập createdAt, để updatedAt null và trả DTO đầy đủ.
     */
    // Create Role - UTCID01
    @Test
    @DisplayName("Create Role - UTCID01")
    void createRole_withValidRequestSavesRoleAndReturnsDto() {
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000014");
        RoleRequestDTO request = createRequest(
                "ADMIN",
                "Administrator",
                "Manage users and roles"
        );
        stubSuccessfulCreate("ADMIN", savedId);
        LocalDateTime beforeCreate = LocalDateTime.now();

        RoleResponseDTO result = assertResponse(
                roleController.createRole(request),
                HttpStatus.CREATED,
                "Role created successfully"
        );
        assertThat(result).isNotNull();

        LocalDateTime afterCreate = LocalDateTime.now();
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).existsByRoleCodeIgnoreCase("ADMIN");
        verify(roleRepository).save(roleCaptor.capture());
        Role savedRole = roleCaptor.getValue();
        assertThat(savedRole.getRoleCode()).isEqualTo("ADMIN");
        assertThat(savedRole.getRoleName()).isEqualTo("Administrator");
        assertThat(savedRole.getRoleDescription()).isEqualTo("Manage users and roles");
        assertThat(savedRole.getCreatedAt()).isBetween(beforeCreate, afterCreate);
        assertThat(savedRole.getUpdatedAt()).isNull();
        assertThat(result.getId()).isEqualTo(savedId);
        assertThat(result.getRoleCode()).isEqualTo("ADMIN");
        assertThat(result.getRoleName()).isEqualTo("Administrator");
        assertThat(result.getRoleDescription()).isEqualTo("Manage users and roles");
        assertThat(result.getCreatedAt()).isEqualTo(savedRole.getCreatedAt());
        assertThat(result.getUpdatedAt()).isNull();
    }

    /**
     * Chuẩn hóa role code viết thường khi tạo mới.
     * Input: code = "project_manager".
     * Expected: HTTP 201, body.status và message thành công;
     * kiểm tra trùng và lưu role bằng code "PROJECT_MANAGER".
     */
    // Create Role - UTCID02
    @Test
    @DisplayName("Create Role - UTCID02")
    void createRole_withLowercaseCodeNormalizesCodeToUppercase() {
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000015");
        RoleRequestDTO request = createRequest(
                "project_manager",
                "Project Manager",
                "Manage projects"
        );
        stubSuccessfulCreate("PROJECT_MANAGER", savedId);

        RoleResponseDTO result = assertResponse(
                roleController.createRole(request),
                HttpStatus.CREATED,
                "Role created successfully"
        );
        assertThat(result).isNotNull();

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).existsByRoleCodeIgnoreCase("PROJECT_MANAGER");
        verify(roleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRoleCode()).isEqualTo("PROJECT_MANAGER");
        assertThat(result.getRoleCode()).isEqualTo("PROJECT_MANAGER");
    }

    /**
     * Loại bỏ khoảng trắng đầu và cuối role name.
     * Input: name = "  Project Manager  ".
     * Expected: HTTP 201, body.status và message thành công;
     * entity và DTO lưu name = "Project Manager".
     */
    // Create Role - UTCID03
    @Test
    @DisplayName("Create Role - UTCID03")
    void createRole_withPaddedNameTrimsRoleName() {
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000016");
        RoleRequestDTO request = createRequest(
                "PROJECT_MANAGER",
                "  Project Manager  ",
                "Manage projects"
        );
        stubSuccessfulCreate("PROJECT_MANAGER", savedId);

        RoleResponseDTO result = assertResponse(
                roleController.createRole(request),
                HttpStatus.CREATED,
                "Role created successfully"
        );
        assertThat(result).isNotNull();

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRoleName()).isEqualTo("Project Manager");
        assertThat(result.getRoleName()).isEqualTo("Project Manager");
    }

    /**
     * Loại bỏ khoảng trắng đầu và cuối description.
     * Input: description = "  Manage projects  ".
     * Expected: HTTP 201, body.status và message thành công;
     * entity và DTO lưu description = "Manage projects".
     */
    // Create Role - UTCID04
    @Test
    @DisplayName("Create Role - UTCID04")
    void createRole_withPaddedDescriptionTrimsDescription() {
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000017");
        RoleRequestDTO request = createRequest(
                "PROJECT_MANAGER",
                "Project Manager",
                "  Manage projects  "
        );
        stubSuccessfulCreate("PROJECT_MANAGER", savedId);

        RoleResponseDTO result = assertResponse(
                roleController.createRole(request),
                HttpStatus.CREATED,
                "Role created successfully"
        );
        assertThat(result).isNotNull();

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRoleDescription()).isEqualTo("Manage projects");
        assertThat(result.getRoleDescription()).isEqualTo("Manage projects");
    }

    /**
     * Tạo Role khi description là null.
     * Input: description = null.
     * Expected: HTTP 201, body.status và message thành công;
     * tạo thành công và description trong entity, DTO đều là null.
     */
    // Create Role - UTCID05
    @Test
    @DisplayName("Create Role - UTCID05")
    void createRole_withNullDescriptionKeepsDescriptionNull() {
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000018");
        RoleRequestDTO request = createRequest("EMPLOYEE", "Employee", null);
        stubSuccessfulCreate("EMPLOYEE", savedId);

        RoleResponseDTO result = assertResponse(
                roleController.createRole(request),
                HttpStatus.CREATED,
                "Role created successfully"
        );
        assertThat(result).isNotNull();

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRoleDescription()).isNull();
        assertThat(result.getRoleDescription()).isNull();
    }

    /**
     * Tạo Role khi description chỉ chứa khoảng trắng.
     * Input: description = "   ".
     * Expected: HTTP 201, body.status và message thành công;
     * chuẩn hóa description thành null trước khi lưu và trả DTO.
     */
    // Create Role - UTCID06
    @Test
    @DisplayName("Create Role - UTCID06")
    void createRole_withBlankDescriptionNormalizesDescriptionToNull() {
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000019");
        RoleRequestDTO request = createRequest("EMPLOYEE", "Employee", "   ");
        stubSuccessfulCreate("EMPLOYEE", savedId);

        RoleResponseDTO result = assertResponse(
                roleController.createRole(request),
                HttpStatus.CREATED,
                "Role created successfully"
        );
        assertThat(result).isNotNull();

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRoleDescription()).isNull();
        assertThat(result.getRoleDescription()).isNull();
    }

    /**
     * Không cho phép tạo Role có code đã tồn tại.
     * Input: code = "admin"; repository xác nhận code "ADMIN" đã tồn tại.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Create Role - UTCID07
    @Test
    @DisplayName("Create Role - UTCID07")
    void createRole_whenCodeAlreadyExistsReturnsBadRequest() {
        RoleRequestDTO request = createRequest(
                "admin",
                "Another Administrator",
                "Duplicate role"
        );
        when(roleRepository.existsByRoleCodeIgnoreCase("ADMIN")).thenReturn(true);

        assertErrorResponse(
                roleController.createRole(request),
                HttpStatus.BAD_REQUEST,
                "Role code is already in use!"
        );
        verify(roleRepository).existsByRoleCodeIgnoreCase("ADMIN");
        verify(roleRepository, never()).save(any(Role.class));
    }

    /**
     * Không cho phép request tạo Role là null.
     * Input: request = null.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Create Role - UTCID08
    @Test
    @DisplayName("Create Role - UTCID08")
    void createRole_withNullRequestReturnsBadRequest() {
        assertCreateValidationFailure(null, "Role request is required");
    }

    /**
     * Không cho phép role code là null.
     * Input: code = null.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Create Role - UTCID09
    @Test
    @DisplayName("Create Role - UTCID09")
    void createRole_withNullCodeReturnsBadRequest() {
        RoleRequestDTO request = createRequest(null, "Manager", "Manage team");

        assertCreateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * Không cho phép role code rỗng.
     * Input: code = "   ".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Create Role - UTCID10
    @Test
    @DisplayName("Create Role - UTCID10")
    void createRole_withBlankCodeReturnsBadRequest() {
        RoleRequestDTO request = createRequest("   ", "Manager", "Manage team");

        assertCreateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * Không cho phép role code ngắn hơn hai ký tự.
     * Input: code = "A".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Create Role - UTCID11
    @Test
    @DisplayName("Create Role - UTCID11")
    void createRole_withOneCharacterCodeReturnsBadRequest() {
        RoleRequestDTO request = createRequest("A", "Manager", "Manage team");

        assertCreateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * Role code phải bắt đầu bằng chữ cái.
     * Input: code = "1ADMIN".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Create Role - UTCID12
    @Test
    @DisplayName("Create Role - UTCID12")
    void createRole_withCodeStartingWithNumberReturnsBadRequest() {
        RoleRequestDTO request = createRequest("1ADMIN", "Manager", "Manage team");

        assertCreateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * Role code không được chứa ký tự đặc biệt ngoài dấu gạch dưới.
     * Input: code = "PROJECT-MANAGER".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Create Role - UTCID13
    @Test
    @DisplayName("Create Role - UTCID13")
    void createRole_withCodeContainingHyphenReturnsBadRequest() {
        RoleRequestDTO request = createRequest(
                "PROJECT-MANAGER",
                "Project Manager",
                "Manage projects"
        );

        assertCreateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * Chấp nhận role code tại biên nhỏ nhất là hai ký tự.
     * Input: code = "A1".
     * Expected: HTTP 201, body.status và message thành công;
     * tạo thành công và DTO có code "A1".
     */
    // Create Role - UTCID14
    @Test
    @DisplayName("Create Role - UTCID14")
    void createRole_withTwoCharacterCodeSucceeds() {
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000020");
        RoleRequestDTO request = createRequest("A1", "Short Code Role", null);
        stubSuccessfulCreate("A1", savedId);

        RoleResponseDTO result = assertResponse(
                roleController.createRole(request),
                HttpStatus.CREATED,
                "Role created successfully"
        );
        assertThat(result).isNotNull();

        assertThat(result.getRoleCode()).isEqualTo("A1");
        verify(roleRepository).existsByRoleCodeIgnoreCase("A1");
        verify(roleRepository).save(any(Role.class));
    }

    /**
     * Chấp nhận role code tại biên tối đa 50 ký tự.
     * Input: code gồm 50 ký tự hợp lệ.
     * Expected: HTTP 201, body.status và message thành công;
     * tạo thành công và DTO giữ đủ code 50 ký tự.
     */
    // Create Role - UTCID15
    @Test
    @DisplayName("Create Role - UTCID15")
    void createRole_withFiftyCharacterCodeSucceeds() {
        String roleCode = "A" + "B".repeat(49);
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000021");
        RoleRequestDTO request = createRequest(roleCode, "Maximum Code Role", null);
        stubSuccessfulCreate(roleCode, savedId);

        RoleResponseDTO result = assertResponse(
                roleController.createRole(request),
                HttpStatus.CREATED,
                "Role created successfully"
        );
        assertThat(result).isNotNull();

        assertThat(result.getRoleCode()).hasSize(50).isEqualTo(roleCode);
        verify(roleRepository).existsByRoleCodeIgnoreCase(roleCode);
        verify(roleRepository).save(any(Role.class));
    }

    /**
     * Không cho phép role code vượt quá 50 ký tự.
     * Input: code gồm 51 ký tự.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Create Role - UTCID16
    @Test
    @DisplayName("Create Role - UTCID16")
    void createRole_withFiftyOneCharacterCodeReturnsBadRequest() {
        String roleCode = "A" + "B".repeat(50);
        RoleRequestDTO request = createRequest(roleCode, "Too Long Code Role", null);

        assertCreateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * Không cho phép role name là null.
     * Input: name = null.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Create Role - UTCID17
    @Test
    @DisplayName("Create Role - UTCID17")
    void createRole_withNullNameReturnsBadRequest() {
        RoleRequestDTO request = createRequest("MANAGER", null, "Manage team");

        assertCreateValidationFailure(request, "Role name is required");
    }

    /**
     * Không cho phép role name chỉ chứa khoảng trắng.
     * Input: name = "   ".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Create Role - UTCID18
    @Test
    @DisplayName("Create Role - UTCID18")
    void createRole_withBlankNameReturnsBadRequest() {
        RoleRequestDTO request = createRequest("MANAGER", "   ", "Manage team");

        assertCreateValidationFailure(request, "Role name is required");
    }

    /**
     * Chấp nhận role name tại biên tối đa 100 ký tự.
     * Input: name gồm 100 ký tự.
     * Expected: HTTP 201, body.status và message thành công;
     * tạo thành công và DTO giữ đủ name 100 ký tự.
     */
    // Create Role - UTCID19
    @Test
    @DisplayName("Create Role - UTCID19")
    void createRole_withOneHundredCharacterNameSucceeds() {
        String roleName = "N".repeat(100);
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000022");
        RoleRequestDTO request = createRequest("MANAGER", roleName, null);
        stubSuccessfulCreate("MANAGER", savedId);

        RoleResponseDTO result = assertResponse(
                roleController.createRole(request),
                HttpStatus.CREATED,
                "Role created successfully"
        );
        assertThat(result).isNotNull();

        assertThat(result.getRoleName()).hasSize(100).isEqualTo(roleName);
        verify(roleRepository).save(any(Role.class));
    }

    /**
     * Không cho phép role name vượt quá 100 ký tự.
     * Input: name gồm 101 ký tự.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Create Role - UTCID20
    @Test
    @DisplayName("Create Role - UTCID20")
    void createRole_withOneHundredOneCharacterNameReturnsBadRequest() {
        RoleRequestDTO request = createRequest(
                "MANAGER",
                "N".repeat(101),
                null
        );

        assertCreateValidationFailure(
                request,
                "Role name cannot exceed 100 characters"
        );
    }

    /**
     * Chấp nhận description tại biên tối đa 255 ký tự.
     * Input: description gồm 255 ký tự.
     * Expected: HTTP 201, body.status và message thành công;
     * tạo thành công và DTO giữ đủ description 255 ký tự.
     */
    // Create Role - UTCID21
    @Test
    @DisplayName("Create Role - UTCID21")
    void createRole_withTwoHundredFiftyFiveCharacterDescriptionSucceeds() {
        String description = "D".repeat(255);
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000023");
        RoleRequestDTO request = createRequest("MANAGER", "Manager", description);
        stubSuccessfulCreate("MANAGER", savedId);

        RoleResponseDTO result = assertResponse(
                roleController.createRole(request),
                HttpStatus.CREATED,
                "Role created successfully"
        );
        assertThat(result).isNotNull();

        assertThat(result.getRoleDescription()).hasSize(255).isEqualTo(description);
        verify(roleRepository).save(any(Role.class));
    }

    /**
     * Không cho phép description vượt quá 255 ký tự.
     * Input: description gồm 256 ký tự.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Create Role - UTCID22
    @Test
    @DisplayName("Create Role - UTCID22")
    void createRole_withTwoHundredFiftySixCharacterDescriptionReturnsBadRequest() {
        RoleRequestDTO request = createRequest(
                "MANAGER",
                "Manager",
                "D".repeat(256)
        );

        assertCreateValidationFailure(
                request,
                "Role description cannot exceed 255 characters"
        );
    }

    /**
     * Repository phát sinh lỗi khi lưu Role hợp lệ.
     * Input: request ADMIN hợp lệ; repository ném RuntimeException "Database unavailable" khi save.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Create Role - UTCID23
    @Test
    @DisplayName("Create Role - UTCID23")
    void createRole_whenRepositorySaveFailsReturnsBadRequest() {
        RoleRequestDTO request = createRequest(
                "ADMIN",
                "Administrator",
                "Manage users and roles"
        );
        RuntimeException repositoryException =
                new RuntimeException("Database unavailable");
        when(roleRepository.existsByRoleCodeIgnoreCase("ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenThrow(repositoryException);

        assertErrorResponse(
                roleController.createRole(request),
                HttpStatus.BAD_REQUEST,
                "Database unavailable"
        );
        verify(roleRepository).existsByRoleCodeIgnoreCase("ADMIN");
        verify(roleRepository).save(any(Role.class));
    }

    /**
     * Từ chối code là chuỗi rỗng, phân biệt với null và chuỗi toàn khoảng trắng.
     * Input: code = "", name = "Manager", description = "Manage team".
     * Expected: HTTP 400, message lỗi định dạng code, data = null; không gọi repository.
     */
    // Create Role - UTCID24
    @Test
    @DisplayName("Create Role - UTCID24")
    void createRole_withEmptyCodeReturnsBadRequest() {
        assertCreateValidationFailure(
                createRequest("", "Manager", "Manage team"),
                invalidRoleCodeMessage()
        );
    }

    /**
     * Từ chối name là chuỗi rỗng.
     * Input: code = "MANAGER", name = "", description = "Manage team".
     * Expected: HTTP 400, message "Role name is required", data = null; không gọi repository.
     */
    // Create Role - UTCID25
    @Test
    @DisplayName("Create Role - UTCID25")
    void createRole_withEmptyNameReturnsBadRequest() {
        assertCreateValidationFailure(
                createRequest("MANAGER", "", "Manage team"),
                "Role name is required"
        );
    }

    /**
     * Chấp nhận description là chuỗi rỗng và chuẩn hóa thành null.
     * Input: code = "EMPLOYEE", name = "Employee", description = ""; code chưa tồn tại.
     * Expected: HTTP 201; description trong entity lưu và DTO đều là null.
     */
    // Create Role - UTCID26
    @Test
    @DisplayName("Create Role - UTCID26")
    void createRole_withEmptyDescriptionNormalizesDescriptionToNull() {
        assertCreatedRoleFields(
                createRequest("EMPLOYEE", "Employee", ""),
                "EMPLOYEE", "Employee", null
        );
    }

    /**
     * Giới hạn code được áp dụng sau khi trim, không tính khoảng trắng hai đầu.
     * Input: " " + code 50 ký tự + " " (tổng 52 ký tự); code chưa tồn tại.
     * Expected: HTTP 201; kiểm tra trùng, lưu và trả đúng code 50 ký tự đã trim.
     */
    // Create Role - UTCID27
    @Test
    @DisplayName("Create Role - UTCID27")
    void createRole_withPaddedFiftyCharacterCodeSucceeds() {
        String roleCode = "A" + "B".repeat(49);

        assertCreatedRoleFields(
                createRequest(" " + roleCode + " ", "Manager", null),
                roleCode, "Manager", null
        );
    }

    /**
     * Không bỏ qua giới hạn code khi input có khoảng trắng hai đầu.
     * Input: " " + code 51 ký tự + " " (tổng 53 ký tự).
     * Expected: HTTP 400, message lỗi định dạng code, data = null; không gọi repository.
     */
    // Create Role - UTCID28
    @Test
    @DisplayName("Create Role - UTCID28")
    void createRole_withPaddedFiftyOneCharacterCodeReturnsBadRequest() {
        String roleCode = "A" + "B".repeat(50);

        assertCreateValidationFailure(
                createRequest(" " + roleCode + " ", "Manager", null),
                invalidRoleCodeMessage()
        );
    }

    /**
     * Giới hạn name được áp dụng sau khi trim.
     * Input: name = " " + "N" lặp 100 lần + " " (102 ký tự); code MANAGER chưa tồn tại.
     * Expected: HTTP 201; entity và DTO giữ đúng name 100 ký tự, không còn khoảng trắng hai đầu.
     */
    // Create Role - UTCID29
    @Test
    @DisplayName("Create Role - UTCID29")
    void createRole_withPaddedOneHundredCharacterNameSucceeds() {
        String roleName = "N".repeat(100);

        assertCreatedRoleFields(
                createRequest("MANAGER", " " + roleName + " ", null),
                "MANAGER", roleName, null
        );
    }

    /**
     * Từ chối name vẫn quá dài sau khi trim.
     * Input: name = " " + "N" lặp 101 lần + " " (103 ký tự).
     * Expected: HTTP 400, message "Role name cannot exceed 100 characters"; không gọi repository.
     */
    // Create Role - UTCID30
    @Test
    @DisplayName("Create Role - UTCID30")
    void createRole_withPaddedOneHundredOneCharacterNameReturnsBadRequest() {
        assertCreateValidationFailure(
                createRequest("MANAGER", " " + "N".repeat(101) + " ", null),
                "Role name cannot exceed 100 characters"
        );
    }

    /**
     * Giới hạn description được áp dụng sau khi trim.
     * Input: description = " " + "D" lặp 255 lần + " " (257 ký tự); code MANAGER chưa tồn tại.
     * Expected: HTTP 201; entity và DTO giữ đúng description 255 ký tự đã trim.
     */
    // Create Role - UTCID31
    @Test
    @DisplayName("Create Role - UTCID31")
    void createRole_withPaddedTwoHundredFiftyFiveCharacterDescriptionSucceeds() {
        String description = "D".repeat(255);

        assertCreatedRoleFields(
                createRequest("MANAGER", "Manager", " " + description + " "),
                "MANAGER", "Manager", description
        );
    }

    /**
     * Từ chối description vẫn quá dài sau khi trim.
     * Input: description = " " + "D" lặp 256 lần + " " (258 ký tự).
     * Expected: HTTP 400, message "Role description cannot exceed 255 characters"; không gọi repository.
     */
    // Create Role - UTCID32
    @Test
    @DisplayName("Create Role - UTCID32")
    void createRole_withPaddedTwoHundredFiftySixCharacterDescriptionReturnsBadRequest() {
        assertCreateValidationFailure(
                createRequest("MANAGER", "Manager", " " + "D".repeat(256) + " "),
                "Role description cannot exceed 255 characters"
        );
    }

    /**
     * Dấu gạch dưới được phép trong code nhưng không được đứng đầu.
     * Input: code = "_ADMIN", name = "Administrator", description = null.
     * Expected: HTTP 400, message lỗi định dạng code, data = null; không gọi repository.
     */
    // Create Role - UTCID33
    @Test
    @DisplayName("Create Role - UTCID33")
    void createRole_withCodeStartingWithUnderscoreReturnsBadRequest() {
        assertCreateValidationFailure(
                createRequest("_ADMIN", "Administrator", null),
                invalidRoleCodeMessage()
        );
    }

    /**
     * Không cho phép khoảng trắng nằm bên trong code.
     * Input: code = "PROJECT MANAGER", name = "Project Manager", description = null.
     * Expected: HTTP 400, message lỗi định dạng code, data = null; không gọi repository.
     */
    // Create Role - UTCID34
    @Test
    @DisplayName("Create Role - UTCID34")
    void createRole_withCodeContainingInternalSpaceReturnsBadRequest() {
        assertCreateValidationFailure(
                createRequest("PROJECT MANAGER", "Project Manager", null),
                invalidRoleCodeMessage()
        );
    }

    /**
     * Code chỉ chấp nhận chữ cái A-Z, không chấp nhận chữ tiếng Việt có dấu.
     * Input: code = "QUẢN_LÝ", name = "Manager", description = null.
     * Expected: HTTP 400, message lỗi định dạng code, data = null; không gọi repository.
     */
    // Create Role - UTCID35
    @Test
    @DisplayName("Create Role - UTCID35")
    void createRole_withAccentedCodeReturnsBadRequest() {
        assertCreateValidationFailure(
                createRequest("QUẢN_LÝ", "Manager", null),
                invalidRoleCodeMessage()
        );
    }

    /**
     * Kết hợp trim và đổi code viết thường sang viết hoa trước khi kiểm tra trùng.
     * Input: code = "  admin  ", name = "Administrator"; repository xác nhận ADMIN chưa tồn tại.
     * Expected: HTTP 201; kiểm tra trùng bằng ADMIN, entity và DTO có code ADMIN.
     */
    // Create Role - UTCID36
    @Test
    @DisplayName("Create Role - UTCID36")
    void createRole_withPaddedLowercaseCodeNormalizesBeforeCheckingDuplicates() {
        assertCreatedRoleFields(
                createRequest("  admin  ", "Administrator", null),
                "ADMIN", "Administrator", null
        );
    }

    /**
     * Khoảng trắng và chữ thường không được giúp vượt qua kiểm tra code trùng.
     * Input: code = "  admin  "; existsByRoleCodeIgnoreCase("ADMIN") trả true.
     * Expected: HTTP 400, message "Role code is already in use!", data = null; không lưu role.
     */
    // Create Role - UTCID37
    @Test
    @DisplayName("Create Role - UTCID37")
    void createRole_withPaddedLowercaseDuplicateCodeReturnsBadRequest() {
        RoleRequestDTO request = createRequest("  admin  ", "Administrator", null);
        when(roleRepository.existsByRoleCodeIgnoreCase("ADMIN")).thenReturn(true);

        assertErrorResponse(
                roleController.createRole(request),
                HttpStatus.BAD_REQUEST,
                "Role code is already in use!"
        );

        verify(roleRepository).existsByRoleCodeIgnoreCase("ADMIN");
        verify(roleRepository, never()).save(any(Role.class));
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Cập nhật Role thành công với dữ liệu hợp lệ.
     * Input: Role ADMIN tồn tại; cập nhật name và description nhưng giữ nguyên code.
     * Expected: HTTP 200, body.status và message thành công;
     * giữ id, code, createdAt; cập nhật dữ liệu, updatedAt và trả DTO đầy đủ.
     */
    // Update Role - UTCID01
    @Test
    @DisplayName("Update Role - UTCID01")
    void updateRole_withValidRequestUpdatesRoleAndReturnsDto() {
        UUID roleId = ROLE_ID;
        LocalDateTime originalCreatedAt = LocalDateTime.of(2026, 8, 20, 9, 15);
        Role existingRole = role(
                roleId.toString(),
                "ADMIN",
                "Administrator",
                "Old description",
                originalCreatedAt,
                LocalDateTime.of(2026, 8, 25, 14, 30)
        );
        RoleRequestDTO request = createRequest(
                "ADMIN",
                "Senior Administrator",
                "Manage users, roles and permissions"
        );
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");
        LocalDateTime beforeUpdate = LocalDateTime.now();

        RoleResponseDTO result = assertResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.OK,
                "Role updated successfully"
        );
        assertThat(result).isNotNull();

        LocalDateTime afterUpdate = LocalDateTime.now();
        assertThat(existingRole.getId()).isEqualTo(roleId);
        assertThat(existingRole.getRoleCode()).isEqualTo("ADMIN");
        assertThat(existingRole.getRoleName()).isEqualTo("Senior Administrator");
        assertThat(existingRole.getRoleDescription())
                .isEqualTo("Manage users, roles and permissions");
        assertThat(existingRole.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(existingRole.getUpdatedAt()).isBetween(beforeUpdate, afterUpdate);
        assertThat(result.getId()).isEqualTo(roleId);
        assertThat(result.getRoleCode()).isEqualTo("ADMIN");
        assertThat(result.getRoleName()).isEqualTo("Senior Administrator");
        assertThat(result.getRoleDescription())
                .isEqualTo("Manage users, roles and permissions");
        assertThat(result.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(result.getUpdatedAt()).isEqualTo(existingRole.getUpdatedAt());
        verify(roleRepository).findById(roleId);
        verify(roleRepository).existsByRoleCodeIgnoreCaseAndIdNot("ADMIN", roleId);
        verify(roleRepository).save(existingRole);
    }

    /**
     * Chấp nhận code viết thường nếu vẫn là code hiện tại.
     * Input: Role hiện có code "ADMIN"; request code = "admin".
     * Expected: HTTP 200, body.status và message thành công;
     * cập nhật thành công, kiểm tra trùng bằng "ADMIN" và giữ code "ADMIN".
     */
    // Update Role - UTCID02
    @Test
    @DisplayName("Update Role - UTCID02")
    void updateRole_withLowercaseCurrentCodeSucceeds() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("admin", "Administrator", null);
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = assertResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.OK,
                "Role updated successfully"
        );
        assertThat(result).isNotNull();

        assertThat(result.getRoleCode()).isEqualTo("ADMIN");
        verify(roleRepository).existsByRoleCodeIgnoreCaseAndIdNot("ADMIN", roleId);
        verify(roleRepository).save(existingRole);
    }

    /**
     * Loại bỏ khoảng trắng quanh code trước khi so sánh.
     * Input: Role hiện có code "ADMIN"; request code = "  ADMIN  ".
     * Expected: HTTP 200, body.status và message thành công;
     * cập nhật thành công và repository nhận code "ADMIN".
     */
    // Update Role - UTCID03
    @Test
    @DisplayName("Update Role - UTCID03")
    void updateRole_withPaddedCurrentCodeSucceeds() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("  ADMIN  ", "Administrator", null);
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = assertResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.OK,
                "Role updated successfully"
        );
        assertThat(result).isNotNull();

        assertThat(result.getRoleCode()).isEqualTo("ADMIN");
        verify(roleRepository).existsByRoleCodeIgnoreCaseAndIdNot("ADMIN", roleId);
        verify(roleRepository).save(existingRole);
    }

    /**
     * Loại bỏ khoảng trắng quanh role name khi cập nhật.
     * Input: name = "  Senior Administrator  ".
     * Expected: HTTP 200, body.status và message thành công;
     * entity và DTO có name = "Senior Administrator".
     */
    // Update Role - UTCID04
    @Test
    @DisplayName("Update Role - UTCID04")
    void updateRole_withPaddedNameTrimsRoleName() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest(
                "ADMIN",
                "  Senior Administrator  ",
                null
        );
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = assertResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.OK,
                "Role updated successfully"
        );
        assertThat(result).isNotNull();

        assertThat(existingRole.getRoleName()).isEqualTo("Senior Administrator");
        assertThat(result.getRoleName()).isEqualTo("Senior Administrator");
        verify(roleRepository).save(existingRole);
    }

    /**
     * Loại bỏ khoảng trắng quanh description khi cập nhật.
     * Input: description = "  Updated description  ".
     * Expected: HTTP 200, body.status và message thành công;
     * entity và DTO có description = "Updated description".
     */
    // Update Role - UTCID05
    @Test
    @DisplayName("Update Role - UTCID05")
    void updateRole_withPaddedDescriptionTrimsDescription() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest(
                "ADMIN",
                "Administrator",
                "  Updated description  "
        );
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = assertResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.OK,
                "Role updated successfully"
        );
        assertThat(result).isNotNull();

        assertThat(existingRole.getRoleDescription()).isEqualTo("Updated description");
        assertThat(result.getRoleDescription()).isEqualTo("Updated description");
        verify(roleRepository).save(existingRole);
    }

    /**
     * Cho phép cập nhật description thành null.
     * Input: description = null.
     * Expected: HTTP 200, body.status và message thành công;
     * cập nhật thành công và description trong entity, DTO đều là null.
     */
    // Update Role - UTCID06
    @Test
    @DisplayName("Update Role - UTCID06")
    void updateRole_withNullDescriptionKeepsDescriptionNull() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("ADMIN", "Administrator", null);
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = assertResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.OK,
                "Role updated successfully"
        );
        assertThat(result).isNotNull();

        assertThat(existingRole.getRoleDescription()).isNull();
        assertThat(result.getRoleDescription()).isNull();
        verify(roleRepository).save(existingRole);
    }

    /**
     * Chuẩn hóa description chỉ chứa khoảng trắng thành null.
     * Input: description = "   ".
     * Expected: HTTP 200, body.status và message thành công;
     * cập nhật thành công và description được lưu là null.
     */
    // Update Role - UTCID07
    @Test
    @DisplayName("Update Role - UTCID07")
    void updateRole_withBlankDescriptionNormalizesDescriptionToNull() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("ADMIN", "Administrator", "   ");
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = assertResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.OK,
                "Role updated successfully"
        );
        assertThat(result).isNotNull();

        assertThat(existingRole.getRoleDescription()).isNull();
        assertThat(result.getRoleDescription()).isNull();
        verify(roleRepository).save(existingRole);
    }

    /**
     * Chấp nhận code hiện tại ở biên tối thiểu hai ký tự.
     * Input: Role và request cùng có code = "A1".
     * Expected: HTTP 200, body.status và message thành công;
     * cập nhật thành công và DTO giữ code "A1".
     */
    // Update Role - UTCID08
    @Test
    @DisplayName("Update Role - UTCID08")
    void updateRole_withTwoCharacterCurrentCodeSucceeds() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "A1");
        RoleRequestDTO request = createRequest("A1", "Short Code Role", null);
        stubSuccessfulUpdate(roleId, existingRole, "A1");

        RoleResponseDTO result = assertResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.OK,
                "Role updated successfully"
        );
        assertThat(result).isNotNull();

        assertThat(result.getRoleCode()).isEqualTo("A1");
        verify(roleRepository).save(existingRole);
    }

    /**
     * Chấp nhận code hiện tại ở biên tối đa 50 ký tự.
     * Input: Role và request cùng có code hợp lệ dài 50 ký tự.
     * Expected: HTTP 200, body.status và message thành công;
     * cập nhật thành công và DTO giữ đủ code 50 ký tự.
     */
    // Update Role - UTCID09
    @Test
    @DisplayName("Update Role - UTCID09")
    void updateRole_withFiftyCharacterCurrentCodeSucceeds() {
        String roleCode = "A" + "B".repeat(49);
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, roleCode);
        RoleRequestDTO request = createRequest(roleCode, "Maximum Code Role", null);
        stubSuccessfulUpdate(roleId, existingRole, roleCode);

        RoleResponseDTO result = assertResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.OK,
                "Role updated successfully"
        );
        assertThat(result).isNotNull();

        assertThat(result.getRoleCode()).hasSize(50).isEqualTo(roleCode);
        verify(roleRepository).save(existingRole);
    }

    /**
     * Chấp nhận role name ở biên tối đa 100 ký tự.
     * Input: name gồm 100 ký tự.
     * Expected: HTTP 200, body.status và message thành công;
     * cập nhật thành công và DTO giữ đủ name 100 ký tự.
     */
    // Update Role - UTCID10
    @Test
    @DisplayName("Update Role - UTCID10")
    void updateRole_withOneHundredCharacterNameSucceeds() {
        String roleName = "N".repeat(100);
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("ADMIN", roleName, null);
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = assertResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.OK,
                "Role updated successfully"
        );
        assertThat(result).isNotNull();

        assertThat(result.getRoleName()).hasSize(100).isEqualTo(roleName);
        verify(roleRepository).save(existingRole);
    }

    /**
     * Chấp nhận description ở biên tối đa 255 ký tự.
     * Input: description gồm 255 ký tự.
     * Expected: HTTP 200, body.status và message thành công;
     * cập nhật thành công và DTO giữ đủ description 255 ký tự.
     */
    // Update Role - UTCID11
    @Test
    @DisplayName("Update Role - UTCID11")
    void updateRole_withTwoHundredFiftyFiveCharacterDescriptionSucceeds() {
        String description = "D".repeat(255);
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("ADMIN", "Administrator", description);
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = assertResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.OK,
                "Role updated successfully"
        );
        assertThat(result).isNotNull();

        assertThat(result.getRoleDescription()).hasSize(255).isEqualTo(description);
        verify(roleRepository).save(existingRole);
    }

    /**
     * Không cho phép request cập nhật là null.
     * Input: request = null.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Update Role - UTCID12
    @Test
    @DisplayName("Update Role - UTCID12")
    void updateRole_withNullRequestReturnsBadRequest() {
        assertUpdateValidationFailure(null, "Role request is required");
    }

    /**
     * Không cho phép role code là null khi cập nhật.
     * Input: code = null.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Update Role - UTCID13
    @Test
    @DisplayName("Update Role - UTCID13")
    void updateRole_withNullCodeReturnsBadRequest() {
        RoleRequestDTO request = createRequest(null, "Administrator", null);

        assertUpdateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * Không cho phép role code rỗng khi cập nhật.
     * Input: code = "   ".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Update Role - UTCID14
    @Test
    @DisplayName("Update Role - UTCID14")
    void updateRole_withBlankCodeReturnsBadRequest() {
        RoleRequestDTO request = createRequest("   ", "Administrator", null);

        assertUpdateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * Không cho phép role code ngắn hơn hai ký tự khi cập nhật.
     * Input: code = "A".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Update Role - UTCID15
    @Test
    @DisplayName("Update Role - UTCID15")
    void updateRole_withOneCharacterCodeReturnsBadRequest() {
        RoleRequestDTO request = createRequest("A", "Administrator", null);

        assertUpdateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * Role code cập nhật phải bắt đầu bằng chữ cái.
     * Input: code = "1ADMIN".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Update Role - UTCID16
    @Test
    @DisplayName("Update Role - UTCID16")
    void updateRole_withCodeStartingWithNumberReturnsBadRequest() {
        RoleRequestDTO request = createRequest("1ADMIN", "Administrator", null);

        assertUpdateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * Role code cập nhật không được chứa dấu gạch ngang.
     * Input: code = "PROJECT-MANAGER".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Update Role - UTCID17
    @Test
    @DisplayName("Update Role - UTCID17")
    void updateRole_withCodeContainingHyphenReturnsBadRequest() {
        RoleRequestDTO request = createRequest(
                "PROJECT-MANAGER",
                "Project Manager",
                null
        );

        assertUpdateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * Không cho phép role code cập nhật vượt quá 50 ký tự.
     * Input: code gồm 51 ký tự.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Update Role - UTCID18
    @Test
    @DisplayName("Update Role - UTCID18")
    void updateRole_withFiftyOneCharacterCodeReturnsBadRequest() {
        RoleRequestDTO request = createRequest(
                "A" + "B".repeat(50),
                "Administrator",
                null
        );

        assertUpdateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * Không cho phép role name là null khi cập nhật.
     * Input: name = null.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Update Role - UTCID19
    @Test
    @DisplayName("Update Role - UTCID19")
    void updateRole_withNullNameReturnsBadRequest() {
        RoleRequestDTO request = createRequest("ADMIN", null, null);

        assertUpdateValidationFailure(request, "Role name is required");
    }

    /**
     * Không cho phép role name rỗng khi cập nhật.
     * Input: name = "   ".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Update Role - UTCID20
    @Test
    @DisplayName("Update Role - UTCID20")
    void updateRole_withBlankNameReturnsBadRequest() {
        RoleRequestDTO request = createRequest("ADMIN", "   ", null);

        assertUpdateValidationFailure(request, "Role name is required");
    }

    /**
     * Không cho phép role name vượt quá 100 ký tự khi cập nhật.
     * Input: name gồm 101 ký tự.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Update Role - UTCID21
    @Test
    @DisplayName("Update Role - UTCID21")
    void updateRole_withOneHundredOneCharacterNameReturnsBadRequest() {
        RoleRequestDTO request = createRequest("ADMIN", "N".repeat(101), null);

        assertUpdateValidationFailure(
                request,
                "Role name cannot exceed 100 characters"
        );
    }

    /**
     * Không cho phép description vượt quá 255 ký tự khi cập nhật.
     * Input: description gồm 256 ký tự.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Update Role - UTCID22
    @Test
    @DisplayName("Update Role - UTCID22")
    void updateRole_withTwoHundredFiftySixCharacterDescriptionReturnsBadRequest() {
        RoleRequestDTO request = createRequest(
                "ADMIN",
                "Administrator",
                "D".repeat(256)
        );

        assertUpdateValidationFailure(
                request,
                "Role description cannot exceed 255 characters"
        );
    }

    /**
     * Cập nhật Role không tồn tại.
     * Input: id không có trong repository và request hợp lệ.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Update Role - UTCID23
    @Test
    @DisplayName("Update Role - UTCID23")
    void updateRole_whenRoleDoesNotExistReturnsBadRequest() {
        UUID roleId = ROLE_ID;
        RoleRequestDTO request = createRequest("ADMIN", "Administrator", null);
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertErrorResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.BAD_REQUEST,
                "Role not found with id: " + roleId
        );
        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).existsByRoleCodeIgnoreCaseAndIdNot(
                any(String.class),
                any(UUID.class)
        );
        verify(roleRepository, never()).save(any(Role.class));
    }

    /**
     * Không cho phép thay đổi role code sau khi tạo.
     * Input: Role hiện có code "ADMIN" nhưng request code = "MANAGER".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi đổi code; entity giữ nguyên tất cả trường, không gọi save.
     */
    // Update Role - UTCID24
    @Test
    @DisplayName("Update Role - UTCID24")
    void updateRole_whenCodeIsChangedReturnsBadRequest() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        Role originalRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("MANAGER", "Administrator", null);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));

        assertErrorResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.BAD_REQUEST,
                "Role code cannot be changed after creation!"
        );
        assertThat(existingRole).usingRecursiveComparison().isEqualTo(originalRole);
        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).existsByRoleCodeIgnoreCaseAndIdNot(
                any(String.class),
                any(UUID.class)
        );
        verify(roleRepository, never()).save(any(Role.class));
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Không cho phép code bị một Role khác sử dụng.
     * Input: Role ADMIN tồn tại; repository báo code ADMIN thuộc id khác.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi trùng code; entity giữ nguyên tất cả trường, không gọi save.
     */
    // Update Role - UTCID25
    @Test
    @DisplayName("Update Role - UTCID25")
    void updateRole_whenCodeBelongsToAnotherRoleReturnsBadRequest() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        Role originalRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("admin", "Administrator", null);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByRoleCodeIgnoreCaseAndIdNot("ADMIN", roleId))
                .thenReturn(true);

        assertErrorResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.BAD_REQUEST,
                "Role code is already in use by another role!"
        );
        assertThat(existingRole).usingRecursiveComparison().isEqualTo(originalRole);
        verify(roleRepository).findById(roleId);
        verify(roleRepository).existsByRoleCodeIgnoreCaseAndIdNot("ADMIN", roleId);
        verify(roleRepository, never()).save(any(Role.class));
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Repository phát sinh lỗi khi tìm Role cần cập nhật.
     * Input: id hợp lệ; findById ném RuntimeException "Database unavailable".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Update Role - UTCID26
    @Test
    @DisplayName("Update Role - UTCID26")
    void updateRole_whenRepositoryFindFailsReturnsBadRequest() {
        UUID roleId = ROLE_ID;
        RoleRequestDTO request = createRequest("ADMIN", "Administrator", null);
        RuntimeException repositoryException =
                new RuntimeException("Database unavailable");
        when(roleRepository.findById(roleId)).thenThrow(repositoryException);

        assertErrorResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.BAD_REQUEST,
                "Database unavailable"
        );
        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).save(any(Role.class));
    }

    /**
     * Repository phát sinh lỗi khi lưu Role đã cập nhật.
     * Input: Role và request hợp lệ; save ném RuntimeException "Database unavailable".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Update Role - UTCID27
    @Test
    @DisplayName("Update Role - UTCID27")
    void updateRole_whenRepositorySaveFailsReturnsBadRequest() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("ADMIN", "Administrator", null);
        RuntimeException repositoryException =
                new RuntimeException("Database unavailable");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByRoleCodeIgnoreCaseAndIdNot("ADMIN", roleId))
                .thenReturn(false);
        when(roleRepository.save(existingRole)).thenThrow(repositoryException);

        assertErrorResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.BAD_REQUEST,
                "Database unavailable"
        );
        verify(roleRepository).findById(roleId);
        verify(roleRepository).existsByRoleCodeIgnoreCaseAndIdNot("ADMIN", roleId);
        verify(roleRepository).save(existingRole);
    }

    /**
     * Từ chối code cập nhật là chuỗi rỗng.
     * Input: id = ROLE_ID, code = "", name = "Administrator", description = null.
     * Expected: HTTP 400, message lỗi định dạng code, data = null; không gọi repository.
     */
    // Update Role - UTCID28
    @Test
    @DisplayName("Update Role - UTCID28")
    void updateRole_withEmptyCodeReturnsBadRequest() {
        assertUpdateValidationFailure(
                createRequest("", "Administrator", null),
                invalidRoleCodeMessage()
        );
    }

    /**
     * Từ chối name cập nhật là chuỗi rỗng.
     * Input: id = ROLE_ID, code = "ADMIN", name = "", description = null.
     * Expected: HTTP 400, message "Role name is required", data = null; không gọi repository.
     */
    // Update Role - UTCID29
    @Test
    @DisplayName("Update Role - UTCID29")
    void updateRole_withEmptyNameReturnsBadRequest() {
        assertUpdateValidationFailure(
                createRequest("ADMIN", "", null),
                "Role name is required"
        );
    }

    /**
     * Cho phép xóa description cũ bằng chuỗi rỗng.
     * Input: id = ROLE_ID; role ADMIN có description "Old description"; request description = "".
     * Expected: HTTP 200; description trong entity và DTO thành null; giữ id, code và createdAt.
     */
    // Update Role - UTCID30
    @Test
    @DisplayName("Update Role - UTCID30")
    void updateRole_withEmptyDescriptionNormalizesDescriptionToNull() {
        assertUpdatedRoleFields(
                createRequest("ADMIN", "Administrator", ""),
                "ADMIN", "Administrator", null
        );
    }

    /**
     * Chấp nhận code hiện tại dài 50 ký tự có thêm khoảng trắng hai đầu.
     * Input: id = ROLE_ID; code hiện tại 50 ký tự; request code = " " + code + " " (52 ký tự).
     * Expected: HTTP 200; giữ code 50 ký tự, kiểm tra trùng bằng code đã trim và cập nhật thành công.
     */
    // Update Role - UTCID31
    @Test
    @DisplayName("Update Role - UTCID31")
    void updateRole_withPaddedFiftyCharacterCurrentCodeSucceeds() {
        String roleCode = "A" + "B".repeat(49);

        assertUpdatedRoleFields(
                createRequest(" " + roleCode + " ", "Administrator", null),
                roleCode, "Administrator", null
        );
    }

    /**
     * Từ chối code cập nhật vẫn quá dài sau khi trim.
     * Input: id = ROLE_ID, code = " " + code 51 ký tự + " " (53 ký tự).
     * Expected: HTTP 400, message lỗi định dạng code, data = null; không gọi repository.
     */
    // Update Role - UTCID32
    @Test
    @DisplayName("Update Role - UTCID32")
    void updateRole_withPaddedFiftyOneCharacterCodeReturnsBadRequest() {
        String roleCode = "A" + "B".repeat(50);

        assertUpdateValidationFailure(
                createRequest(" " + roleCode + " ", "Administrator", null),
                invalidRoleCodeMessage()
        );
    }

    /**
     * Giới hạn name cập nhật được áp dụng sau khi trim.
     * Input: role ADMIN tồn tại; name = " " + "N" lặp 100 lần + " " (102 ký tự).
     * Expected: HTTP 200; entity và DTO giữ name 100 ký tự đã trim; giữ id, code và createdAt.
     */
    // Update Role - UTCID33
    @Test
    @DisplayName("Update Role - UTCID33")
    void updateRole_withPaddedOneHundredCharacterNameSucceeds() {
        String roleName = "N".repeat(100);

        assertUpdatedRoleFields(
                createRequest("ADMIN", " " + roleName + " ", null),
                "ADMIN", roleName, null
        );
    }

    /**
     * Từ chối name cập nhật vẫn quá dài sau khi trim.
     * Input: id = ROLE_ID, name = " " + "N" lặp 101 lần + " " (103 ký tự).
     * Expected: HTTP 400, message "Role name cannot exceed 100 characters"; không gọi repository.
     */
    // Update Role - UTCID34
    @Test
    @DisplayName("Update Role - UTCID34")
    void updateRole_withPaddedOneHundredOneCharacterNameReturnsBadRequest() {
        assertUpdateValidationFailure(
                createRequest("ADMIN", " " + "N".repeat(101) + " ", null),
                "Role name cannot exceed 100 characters"
        );
    }

    /**
     * Giới hạn description cập nhật được áp dụng sau khi trim.
     * Input: role ADMIN tồn tại; description = " " + "D" lặp 255 lần + " " (257 ký tự).
     * Expected: HTTP 200; entity và DTO giữ description 255 ký tự đã trim; giữ id, code và createdAt.
     */
    // Update Role - UTCID35
    @Test
    @DisplayName("Update Role - UTCID35")
    void updateRole_withPaddedTwoHundredFiftyFiveCharacterDescriptionSucceeds() {
        String description = "D".repeat(255);

        assertUpdatedRoleFields(
                createRequest("ADMIN", "Administrator", " " + description + " "),
                "ADMIN", "Administrator", description
        );
    }

    /**
     * Từ chối description cập nhật vẫn quá dài sau khi trim.
     * Input: id = ROLE_ID, description = " " + "D" lặp 256 lần + " " (258 ký tự).
     * Expected: HTTP 400, message "Role description cannot exceed 255 characters"; không gọi repository.
     */
    // Update Role - UTCID36
    @Test
    @DisplayName("Update Role - UTCID36")
    void updateRole_withPaddedTwoHundredFiftySixCharacterDescriptionReturnsBadRequest() {
        assertUpdateValidationFailure(
                createRequest("ADMIN", "Administrator", " " + "D".repeat(256) + " "),
                "Role description cannot exceed 255 characters"
        );
    }

    /**
     * Không cho phép code cập nhật bắt đầu bằng dấu gạch dưới.
     * Input: id = ROLE_ID, code = "_ADMIN", name = "Administrator", description = null.
     * Expected: HTTP 400 do định dạng code, không phải lỗi đổi code; không gọi repository.
     */
    // Update Role - UTCID37
    @Test
    @DisplayName("Update Role - UTCID37")
    void updateRole_withCodeStartingWithUnderscoreReturnsBadRequest() {
        assertUpdateValidationFailure(
                createRequest("_ADMIN", "Administrator", null),
                invalidRoleCodeMessage()
        );
    }

    /**
     * Không cho phép code cập nhật chứa khoảng trắng ở giữa.
     * Input: id = ROLE_ID, code = "PROJECT MANAGER", name = "Project Manager", description = null.
     * Expected: HTTP 400 do định dạng code, data = null; không gọi repository.
     */
    // Update Role - UTCID38
    @Test
    @DisplayName("Update Role - UTCID38")
    void updateRole_withCodeContainingInternalSpaceReturnsBadRequest() {
        assertUpdateValidationFailure(
                createRequest("PROJECT MANAGER", "Project Manager", null),
                invalidRoleCodeMessage()
        );
    }

    /**
     * Không cho phép code cập nhật chứa chữ tiếng Việt có dấu.
     * Input: id = ROLE_ID, code = "QUẢN_LÝ", name = "Manager", description = null.
     * Expected: HTTP 400 do định dạng code, data = null; không gọi repository.
     */
    // Update Role - UTCID39
    @Test
    @DisplayName("Update Role - UTCID39")
    void updateRole_withAccentedCodeReturnsBadRequest() {
        assertUpdateValidationFailure(
                createRequest("QUẢN_LÝ", "Manager", null),
                invalidRoleCodeMessage()
        );
    }

    /**
     * View Role thành công và chuyển đầy đủ entity sang DTO.
     * Input: id = "00000000-0000-0000-0000-000000000001";
     * repository được mock trả ADMIN, name = "Administrator",
     * description = "Manage users and roles", createdAt = 20/08/2026 09:15,
     * updatedAt = 25/08/2026 14:30.
     * Expected: HTTP 200, body.status và message thành công;
     * DTO giữ đúng cả 6 trường; chỉ gọi findById đúng id một lần,
     * không ghi dữ liệu xuống repository.
     */
    // View Role - UTCID01
    @Test
    @DisplayName("View Role - UTCID01")
    void getRoleById_mapsEveryEntityFieldToDto() {
        UUID roleId = ROLE_ID;
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 20, 9, 15);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 25, 14, 30);
        Role admin = role(
                roleId.toString(),
                "ADMIN",
                "Administrator",
                "Manage users and roles",
                createdAt,
                updatedAt
        );
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(admin));

        RoleResponseDTO result = assertResponse(
                roleController.getRoleById(roleId),
                HttpStatus.OK,
                "Role found"
        );
        assertThat(result).isNotNull();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(roleId);
        assertThat(result.getRoleCode()).isEqualTo("ADMIN");
        assertThat(result.getRoleName()).isEqualTo("Administrator");
        assertThat(result.getRoleDescription()).isEqualTo("Manage users and roles");
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
        verify(roleRepository).findById(roleId);
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * View Role thất bại khi repository không tìm thấy id.
     * Input: id = "00000000-0000-0000-0000-000000000001";
     * findById được mock trả Optional.empty().
     * Expected: HTTP 404, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // View Role - UTCID02
    @Test
    @DisplayName("View Role - UTCID02")
    void getRoleById_whenRoleDoesNotExistReturnsNotFound() {
        UUID roleId = ROLE_ID;
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertErrorResponse(
                roleController.getRoleById(roleId),
                HttpStatus.NOT_FOUND,
                "Role not found with id: " + roleId
        );
        verify(roleRepository).findById(roleId);
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * View Role thành công khi description là null.
     * Input: id = "00000000-0000-0000-0000-000000000001";
     * repository trả EMPLOYEE, name = "Employee", description = null,
     * createdAt = 20/08/2026 09:15, updatedAt = 25/08/2026 14:30.
     * Expected: HTTP 200, body.status và message thành công;
     * DTO đúng Role EMPLOYEE, description vẫn null và các trường còn lại
     * giữ nguyên; chỉ đọc repository, không phát sinh exception.
     */
    // View Role - UTCID03
    @Test
    @DisplayName("View Role - UTCID03")
    void getRoleById_withNullDescriptionKeepsDescriptionNull() {
        UUID roleId = ROLE_ID;
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 20, 9, 15);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 25, 14, 30);
        Role employee = role(
                roleId.toString(),
                "EMPLOYEE",
                "Employee",
                null,
                createdAt,
                updatedAt
        );
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(employee));

        RoleResponseDTO result = assertResponse(
                roleController.getRoleById(roleId),
                HttpStatus.OK,
                "Role found"
        );
        assertThat(result).isNotNull();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(roleId);
        assertThat(result.getRoleCode()).isEqualTo("EMPLOYEE");
        assertThat(result.getRoleName()).isEqualTo("Employee");
        assertThat(result.getRoleDescription()).isNull();
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
        verify(roleRepository).findById(roleId);
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * View Role chưa từng được cập nhật.
     * Input: id = "00000000-0000-0000-0000-000000000001";
     * repository trả ADMIN, name = "Administrator",
     * description = "Manage users and roles", createdAt = 20/08/2026 09:15,
     * updatedAt = null.
     * Expected: HTTP 200, body.status và message thành công;
     * DTO đúng Role ADMIN, updatedAt vẫn null và các trường còn lại
     * giữ nguyên; chỉ đọc repository, không phát sinh exception.
     */
    // View Role - UTCID04
    @Test
    @DisplayName("View Role - UTCID04")
    void getRoleById_withNullUpdatedAtKeepsUpdatedAtNull() {
        UUID roleId = ROLE_ID;
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 20, 9, 15);
        Role admin = role(
                roleId.toString(),
                "ADMIN",
                "Administrator",
                "Manage users and roles",
                createdAt,
                null
        );
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(admin));

        RoleResponseDTO result = assertResponse(
                roleController.getRoleById(roleId),
                HttpStatus.OK,
                "Role found"
        );
        assertThat(result).isNotNull();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(roleId);
        assertThat(result.getRoleCode()).isEqualTo("ADMIN");
        assertThat(result.getRoleName()).isEqualTo("Administrator");
        assertThat(result.getRoleDescription()).isEqualTo("Manage users and roles");
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isNull();
        verify(roleRepository).findById(roleId);
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Repository phát sinh lỗi khi View Role.
     * Input: id = "00000000-0000-0000-0000-000000000001";
     * findById được mock ném RuntimeException "Database unavailable".
     * Expected: HTTP 404, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // View Role - UTCID05
    @Test
    @DisplayName("View Role - UTCID05")
    void getRoleById_whenRepositoryFindFailsReturnsNotFound() {
        UUID roleId = ROLE_ID;
        RuntimeException repositoryException =
                new RuntimeException("Database unavailable");
        when(roleRepository.findById(roleId)).thenThrow(repositoryException);

        assertErrorResponse(
                roleController.getRoleById(roleId),
                HttpStatus.NOT_FOUND,
                "Database unavailable"
        );
        verify(roleRepository).findById(roleId);
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Xóa Role thành công tại biên không có user nào được gán.
     * Input: id = "00000000-0000-0000-0000-000000000001"; Role ADMIN tồn tại;
     * repository.countAssignedUsers(id) trả 0.
     * Expected: HTTP 200, body.status và message thành công;
     * không ném exception; lần lượt tìm Role, kiểm tra số user và xóa đúng
     * entity vừa tìm được, mỗi thao tác một lần; không gọi thêm phương thức repository.
     */
    // Delete Role - UTCID01
    @Test
    @DisplayName("Delete Role - UTCID01")
    void deleteRole_withZeroAssignedUsersDeletesExistingRole() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.countAssignedUsers(roleId)).thenReturn(0L);

        assertThat(assertResponse(
                roleController.deleteRole(roleId),
                HttpStatus.OK,
                "Role deleted successfully"
        )).isNull();

        InOrder repositoryCalls = inOrder(roleRepository);
        repositoryCalls.verify(roleRepository).findById(roleId);
        repositoryCalls.verify(roleRepository).countAssignedUsers(roleId);
        repositoryCalls.verify(roleRepository).delete(same(existingRole));
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Không cho phép xóa Role đang được gán cho đúng một user.
     * Input: id = "00000000-0000-0000-0000-000000000001"; Role ADMIN tồn tại;
     * repository.countAssignedUsers(id) trả 1 (biên nhỏ nhất phải chặn xóa).
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Delete Role - UTCID02
    @Test
    @DisplayName("Delete Role - UTCID02")
    void deleteRole_withOneAssignedUserReturnsBadRequestWithoutDeleting() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.countAssignedUsers(roleId)).thenReturn(1L);

        assertErrorResponse(
                roleController.deleteRole(roleId),
                HttpStatus.BAD_REQUEST,
                "Role cannot be deleted because it is assigned to users!"
        );
        verify(roleRepository).findById(roleId);
        verify(roleRepository).countAssignedUsers(roleId);
        verify(roleRepository, never()).delete(any(Role.class));
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Không cho phép xóa Role đang được gán cho nhiều user.
     * Input: id = "00000000-0000-0000-0000-000000000001"; Role EMPLOYEE tồn tại;
     * repository.countAssignedUsers(id) trả 5.
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Delete Role - UTCID03
    @Test
    @DisplayName("Delete Role - UTCID03")
    void deleteRole_withMultipleAssignedUsersReturnsBadRequestWithoutDeleting() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "EMPLOYEE");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.countAssignedUsers(roleId)).thenReturn(5L);

        assertErrorResponse(
                roleController.deleteRole(roleId),
                HttpStatus.BAD_REQUEST,
                "Role cannot be deleted because it is assigned to users!"
        );
        verify(roleRepository).findById(roleId);
        verify(roleRepository).countAssignedUsers(roleId);
        verify(roleRepository, never()).delete(any(Role.class));
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Xóa Role có id không tồn tại.
     * Input: id = "00000000-0000-0000-0000-000000000001";
     * repository.findById(id) trả Optional.empty().
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Delete Role - UTCID04
    @Test
    @DisplayName("Delete Role - UTCID04")
    void deleteRole_whenRoleDoesNotExistReturnsBadRequestWithoutDeleting() {
        UUID roleId = ROLE_ID;
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertErrorResponse(
                roleController.deleteRole(roleId),
                HttpStatus.BAD_REQUEST,
                "Role not found with id: " + roleId
        );
        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).countAssignedUsers(any(UUID.class));
        verify(roleRepository, never()).delete(any(Role.class));
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Repository gặp lỗi khi tìm Role để xóa.
     * Input: id = "00000000-0000-0000-0000-000000000001";
     * findById ném RuntimeException "Unable to find role".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Delete Role - UTCID05
    @Test
    @DisplayName("Delete Role - UTCID05")
    void deleteRole_whenRepositoryFindFailsReturnsBadRequestWithoutDeleting() {
        UUID roleId = ROLE_ID;
        RuntimeException repositoryException = new RuntimeException("Unable to find role");
        when(roleRepository.findById(roleId)).thenThrow(repositoryException);

        assertErrorResponse(
                roleController.deleteRole(roleId),
                HttpStatus.BAD_REQUEST,
                "Unable to find role"
        );
        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).countAssignedUsers(any(UUID.class));
        verify(roleRepository, never()).delete(any(Role.class));
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Repository gặp lỗi khi kiểm tra số user được gán Role.
     * Input: id = "00000000-0000-0000-0000-000000000001"; Role ADMIN tồn tại;
     * countAssignedUsers ném RuntimeException "Unable to count assigned users".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Delete Role - UTCID06
    @Test
    @DisplayName("Delete Role - UTCID06")
    void deleteRole_whenUserCountFailsReturnsBadRequestWithoutDeleting() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        RuntimeException repositoryException = new RuntimeException("Unable to count assigned users");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.countAssignedUsers(roleId)).thenThrow(repositoryException);

        assertErrorResponse(
                roleController.deleteRole(roleId),
                HttpStatus.BAD_REQUEST,
                "Unable to count assigned users"
        );
        verify(roleRepository).findById(roleId);
        verify(roleRepository).countAssignedUsers(roleId);
        verify(roleRepository, never()).delete(any(Role.class));
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Repository gặp lỗi trong thao tác xóa Role.
     * Input: id = "00000000-0000-0000-0000-000000000001"; Role ADMIN tồn tại;
     * countAssignedUsers trả 0; delete ném RuntimeException "Unable to delete role".
     * Expected: HTTP 400, body.status khớp HTTP status, data = null,
     * message đúng lỗi nghiệp vụ/repository; kiểm tra các thao tác repository như bên dưới.
     */
    // Delete Role - UTCID07
    @Test
    @DisplayName("Delete Role - UTCID07")
    void deleteRole_whenRepositoryDeleteFailsReturnsBadRequest() {
        UUID roleId = ROLE_ID;
        Role existingRole = existingRole(roleId, "ADMIN");
        RuntimeException repositoryException = new RuntimeException("Unable to delete role");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.countAssignedUsers(roleId)).thenReturn(0L);
        doThrow(repositoryException).when(roleRepository).delete(same(existingRole));

        assertErrorResponse(
                roleController.deleteRole(roleId),
                HttpStatus.BAD_REQUEST,
                "Unable to delete role"
        );
        InOrder repositoryCalls = inOrder(roleRepository);
        repositoryCalls.verify(roleRepository).findById(roleId);
        repositoryCalls.verify(roleRepository).countAssignedUsers(roleId);
        repositoryCalls.verify(roleRepository).delete(same(existingRole));
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Lấy toàn bộ Role khi repository chưa có dữ liệu.
     * Input: không có tham số; repository.searchRoles("") trả [].
     * Expected: HTTP 200, body.status và message thành công;
     * data là danh sách rỗng khác null.
     */
    // Get All Role - UTCID02
    @Test
    @DisplayName("Get All Role - UTCID02")
    void getAllRoles_withNoRolesReturnsEmptyList() {
        when(roleRepository.searchRoles("")).thenReturn(List.of());

        List<RoleResponseDTO> result = assertResponse(
                roleController.getAllRoles(),
                HttpStatus.OK,
                "Successfully fetched all roles"
        );

        assertThat(result).isNotNull().isEmpty();
        verify(roleRepository).searchRoles("");
        verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Lỗi repository khi lấy toàn bộ Role.
     * Input: không có tham số; searchRoles("") ném RuntimeException "Unable to fetch roles".
     * Expected: controller truyền nguyên RuntimeException và message từ repository.
     */
    // Get All Role - UTCID03
    @Test
    @DisplayName("Get All Role - UTCID03")
    void getAllRoles_whenRepositoryFailsPropagatesException() {
        RuntimeException failure = new RuntimeException("Unable to fetch roles");
        when(roleRepository.searchRoles("")).thenThrow(failure);

        assertThatThrownBy(() -> roleController.getAllRoles())
                .isSameAs(failure)
                .hasMessage("Unable to fetch roles");
        verify(roleRepository).searchRoles("");
        verifyNoMoreInteractions(roleRepository);
    }

    private void assertCreatedRoleFields(
            RoleRequestDTO request,
            String expectedCode,
            String expectedName,
            String expectedDescription
    ) {
        stubSuccessfulCreate(expectedCode, ROLE_ID);
        LocalDateTime beforeCreate = LocalDateTime.now();

        RoleResponseDTO result = assertResponse(
                roleController.createRole(request),
                HttpStatus.CREATED,
                "Role created successfully"
        );

        LocalDateTime afterCreate = LocalDateTime.now();
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).existsByRoleCodeIgnoreCase(expectedCode);
        verify(roleRepository).save(roleCaptor.capture());
        Role savedRole = roleCaptor.getValue();
        assertThat(savedRole.getRoleCode()).isEqualTo(expectedCode);
        assertThat(savedRole.getRoleName()).isEqualTo(expectedName);
        assertThat(savedRole.getRoleDescription()).isEqualTo(expectedDescription);
        assertThat(savedRole.getCreatedAt()).isBetween(beforeCreate, afterCreate);
        assertThat(savedRole.getUpdatedAt()).isNull();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ROLE_ID);
        assertThat(result.getRoleCode()).isEqualTo(expectedCode);
        assertThat(result.getRoleName()).isEqualTo(expectedName);
        assertThat(result.getRoleDescription()).isEqualTo(expectedDescription);
        assertThat(result.getCreatedAt()).isEqualTo(savedRole.getCreatedAt());
        assertThat(result.getUpdatedAt()).isNull();
        verifyNoMoreInteractions(roleRepository);
    }

    private void assertUpdatedRoleFields(
            RoleRequestDTO request,
            String existingCode,
            String expectedName,
            String expectedDescription
    ) {
        Role existingRole = existingRole(ROLE_ID, existingCode);
        LocalDateTime originalCreatedAt = existingRole.getCreatedAt();
        stubSuccessfulUpdate(ROLE_ID, existingRole, existingCode);
        LocalDateTime beforeUpdate = LocalDateTime.now();

        RoleResponseDTO result = assertResponse(
                roleController.updateRole(ROLE_ID, request),
                HttpStatus.OK,
                "Role updated successfully"
        );

        LocalDateTime afterUpdate = LocalDateTime.now();
        assertThat(existingRole.getId()).isEqualTo(ROLE_ID);
        assertThat(existingRole.getRoleCode()).isEqualTo(existingCode);
        assertThat(existingRole.getRoleName()).isEqualTo(expectedName);
        assertThat(existingRole.getRoleDescription()).isEqualTo(expectedDescription);
        assertThat(existingRole.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(existingRole.getUpdatedAt()).isBetween(beforeUpdate, afterUpdate);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ROLE_ID);
        assertThat(result.getRoleCode()).isEqualTo(existingCode);
        assertThat(result.getRoleName()).isEqualTo(expectedName);
        assertThat(result.getRoleDescription()).isEqualTo(expectedDescription);
        assertThat(result.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(result.getUpdatedAt()).isEqualTo(existingRole.getUpdatedAt());
        verify(roleRepository).findById(ROLE_ID);
        verify(roleRepository).existsByRoleCodeIgnoreCaseAndIdNot(existingCode, ROLE_ID);
        verify(roleRepository).save(same(existingRole));
        verifyNoMoreInteractions(roleRepository);
    }

    private static <T> T assertResponse(
            ResponseEntity<BaseResponse<T>> response,
            HttpStatus expectedStatus,
            String expectedMessage
    ) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        BaseResponse<T> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(expectedStatus.value());
        assertThat(body.getMessage()).isEqualTo(expectedMessage);
        return body.getData();
    }

    private static void assertErrorResponse(
            ResponseEntity<? extends BaseResponse<?>> response,
            HttpStatus expectedStatus,
            String expectedMessage
    ) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(expectedStatus.value());
        assertThat(response.getBody().getMessage()).isEqualTo(expectedMessage);
        assertThat(response.getBody().getData()).isNull();
    }

    private void stubSuccessfulUpdate(
            UUID roleId,
            Role existingRole,
            String normalizedRoleCode
    ) {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByRoleCodeIgnoreCaseAndIdNot(
                normalizedRoleCode,
                roleId
        )).thenReturn(false);
        when(roleRepository.save(existingRole)).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void assertUpdateValidationFailure(
            RoleRequestDTO request,
            String expectedMessage
    ) {
        UUID roleId = ROLE_ID;
        assertErrorResponse(
                roleController.updateRole(roleId, request),
                HttpStatus.BAD_REQUEST,
                expectedMessage
        );
        verifyNoInteractions(roleRepository);
    }

    private static Role existingRole(UUID roleId, String roleCode) {
        return role(
                roleId.toString(),
                roleCode,
                "Administrator",
                "Old description",
                LocalDateTime.of(2026, 8, 20, 9, 15),
                LocalDateTime.of(2026, 8, 25, 14, 30)
        );
    }

    private void stubSuccessfulCreate(String normalizedRoleCode, UUID savedId) {
        when(roleRepository.existsByRoleCodeIgnoreCase(normalizedRoleCode))
                .thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role savedRole = invocation.getArgument(0);
            savedRole.setId(savedId);
            return savedRole;
        });
    }

    private void assertCreateValidationFailure(
            RoleRequestDTO request,
            String expectedMessage
    ) {
        assertErrorResponse(
                roleController.createRole(request),
                HttpStatus.BAD_REQUEST,
                expectedMessage
        );
        verifyNoInteractions(roleRepository);
    }

    private static String invalidRoleCodeMessage() {
        return "Role code must contain 2-50 uppercase letters, numbers, or underscores";
    }

    private static RoleRequestDTO createRequest(
            String roleCode,
            String roleName,
            String roleDescription
    ) {
        return new RoleRequestDTO(roleCode, roleName, roleDescription);
    }

    private static Role role(
            String id,
            String roleCode,
            String roleName,
            String roleDescription,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        Role role = new Role();
        role.setId(UUID.fromString(id));
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setRoleDescription(roleDescription);
        role.setCreatedAt(createdAt);
        role.setUpdatedAt(updatedAt);
        return role;
    }
}
