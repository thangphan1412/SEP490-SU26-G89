package com.fpt.backend.service.impl.role;

import com.fpt.backend.dto.request.role.RoleRequestDTO;
import com.fpt.backend.dto.response.role.RoleResponseDTO;
import com.fpt.backend.entity.Role;
import com.fpt.backend.repository.role.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    /**
     * TC01 - Tìm kiếm bằng từ khóa viết hoa toàn bộ.
     * Input: search = "ADMIN"; repository được cấu hình nhận "admin".
     * Expected: service chuyển input thành chữ thường và trả một DTO ADMIN.
     */
    @Test
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

        List<RoleResponseDTO> result = roleService.searchRoles("ADMIN");

        assertThat(result).singleElement().satisfies(dto ->
                assertThat(dto.getRoleCode()).isEqualTo("ADMIN")
        );
        verify(roleRepository).searchRoles("admin");
    }

    /**
     * TC02 - Tìm kiếm bằng từ khóa đã viết thường.
     * Input: search = "admin"; repository được gọi với chính từ khóa "admin".
     * Expected: service trả một DTO ADMIN mà không làm thay đổi từ khóa hợp lệ.
     */
    @Test
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

        List<RoleResponseDTO> result = roleService.searchRoles("admin");

        assertThat(result).singleElement().satisfies(dto ->
                assertThat(dto.getRoleName()).isEqualTo("Administrator")
        );
        verify(roleRepository).searchRoles("admin");
    }

    /**
     * TC03 - Tìm kiếm bằng từ khóa có chữ hoa và chữ thường xen kẽ.
     * Input: search = "AdMiN"; repository được cấu hình nhận "admin".
     * Expected: service chuyển toàn bộ từ khóa thành chữ thường và trả DTO ADMIN.
     */
    @Test
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

        List<RoleResponseDTO> result = roleService.searchRoles("AdMiN");

        assertThat(result).singleElement().satisfies(dto ->
                assertThat(dto.getRoleCode()).isEqualTo("ADMIN")
        );
        verify(roleRepository).searchRoles("admin");
    }

    /**
     * TC04 - Tìm kiếm bằng từ khóa có khoảng trắng ở đầu và cuối.
     * Input: search = "  admin  "; repository được cấu hình nhận "admin".
     * Expected: service loại bỏ khoảng trắng và trả một DTO ADMIN.
     */
    @Test
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

        List<RoleResponseDTO> result = roleService.searchRoles("  admin  ");

        assertThat(result).singleElement().satisfies(dto ->
                assertThat(dto.getRoleCode()).isEqualTo("ADMIN")
        );
        verify(roleRepository).searchRoles("admin");
    }

    /**
     * TC05 - Chuyển đầy đủ dữ liệu Role entity sang RoleResponseDTO.
     * Input: search = "admin"; repository trả Role có đủ id, code, name, description và ngày.
     * Expected: mọi trường dữ liệu trong DTO giống với Role entity ban đầu.
     */
    @Test
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

        List<RoleResponseDTO> result = roleService.searchRoles("admin");

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
     * TC06 - Giữ nguyên thứ tự Role do repository trả về.
     * Input: search = "admin"; repository trả SYSTEM_ADMIN trước ADMIN.
     * Expected: danh sách DTO cũng có thứ tự SYSTEM_ADMIN rồi ADMIN.
     */
    @Test
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

        List<RoleResponseDTO> result = roleService.searchRoles("admin");

        assertThat(result)
                .extracting(RoleResponseDTO::getRoleCode)
                .containsExactly("SYSTEM_ADMIN", "ADMIN");
        verify(roleRepository).searchRoles("admin");
    }

    /**
     * TC07 - Search bằng chuỗi rỗng.
     * Input: search = ""; repository nhận "" và trả Role CEO, EMPLOYEE.
     * Expected: service trả hai DTO CEO và EMPLOYEE.
     */
    @Test
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

        List<RoleResponseDTO> result = roleService.searchRoles("");

        assertThat(result)
                .extracting(RoleResponseDTO::getRoleCode)
                .containsExactly("CEO", "EMPLOYEE");
        verify(roleRepository).searchRoles("");
    }

    /**
     * TC08 - Search chỉ chứa khoảng trắng.
     * Input: search = "   "; repository được cấu hình nhận chuỗi rỗng "".
     * Expected: service loại bỏ khoảng trắng và trả một DTO CEO.
     */
    @Test
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

        List<RoleResponseDTO> result = roleService.searchRoles("   ");

        assertThat(result).singleElement().satisfies(dto ->
                assertThat(dto.getRoleCode()).isEqualTo("CEO")
        );
        verify(roleRepository).searchRoles("");
    }

    /**
     * TC09 - Search có giá trị null.
     * Input: search = null; repository được cấu hình nhận chuỗi rỗng "".
     * Expected: service không phát sinh lỗi và trả một DTO EMPLOYEE.
     */
    @Test
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

        List<RoleResponseDTO> result = roleService.searchRoles(null);

        assertThat(result).singleElement().satisfies(dto -> {
            assertThat(dto.getRoleCode()).isEqualTo("EMPLOYEE");
            assertThat(dto.getRoleDescription()).isNull();
        });
        verify(roleRepository).searchRoles("");
    }

    /**
     * TC10 - Không có Role phù hợp với từ khóa tìm kiếm.
     * Input: search = "not-found"; repository trả về danh sách rỗng.
     * Expected: service trả danh sách rỗng, không trả null.
     */
    @Test
    void searchRoles_returnsEmptyListWhenNoRoleMatches() {
        String search = "not-found";
        when(roleRepository.searchRoles(search)).thenReturn(List.of());

        List<RoleResponseDTO> result = roleService.searchRoles(search);

        assertThat(result).isNotNull().isEmpty();
        verify(roleRepository).searchRoles(search);
    }

    /**
     * TC11 - Lấy toàn bộ Role qua hàm getAllRoles.
     * Input: không có tham số; repository được gọi với search = "" và trả hai Role.
     * Expected: service trả đúng hai DTO ADMIN và EMPLOYEE.
     */
    @Test
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

        List<RoleResponseDTO> result = roleService.getAllRoles();

        assertThat(result)
                .extracting(RoleResponseDTO::getRoleCode)
                .containsExactly("ADMIN", "EMPLOYEE");
        verify(roleRepository).searchRoles("");
    }

    /**
     * TC12 - Repository phát sinh lỗi khi tìm kiếm Role.
     * Input: search = "admin"; repository ném RuntimeException "Database unavailable".
     * Expected: service truyền nguyên exception ra ngoài để tầng trên xử lý.
     */
    @Test
    void searchRoles_propagatesRepositoryException() {
        String search = "admin";
        RuntimeException repositoryException =
                new RuntimeException("Database unavailable");
        when(roleRepository.searchRoles(search)).thenThrow(repositoryException);

        assertThatThrownBy(() -> roleService.searchRoles(search))
                .isSameAs(repositoryException)
                .hasMessage("Database unavailable");
        verify(roleRepository).searchRoles(search);
    }

    /**
     * TC13 - Tạo Role thành công với đầy đủ dữ liệu hợp lệ.
     * Input: code = "ADMIN", name = "Administrator", description = "Manage users and roles".
     * Expected: lưu đúng entity, thiết lập createdAt, để updatedAt null và trả DTO đầy đủ.
     */
    @Test
    void createRole_withValidRequestSavesRoleAndReturnsDto() {
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000014");
        RoleRequestDTO request = createRequest(
                "ADMIN",
                "Administrator",
                "Manage users and roles"
        );
        stubSuccessfulCreate("ADMIN", savedId);
        LocalDateTime beforeCreate = LocalDateTime.now();

        RoleResponseDTO result = roleService.createRole(request);

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
     * TC14 - Chuẩn hóa role code viết thường khi tạo mới.
     * Input: code = "project_manager".
     * Expected: kiểm tra trùng và lưu role bằng code "PROJECT_MANAGER".
     */
    @Test
    void createRole_withLowercaseCodeNormalizesCodeToUppercase() {
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000015");
        RoleRequestDTO request = createRequest(
                "project_manager",
                "Project Manager",
                "Manage projects"
        );
        stubSuccessfulCreate("PROJECT_MANAGER", savedId);

        RoleResponseDTO result = roleService.createRole(request);

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).existsByRoleCodeIgnoreCase("PROJECT_MANAGER");
        verify(roleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRoleCode()).isEqualTo("PROJECT_MANAGER");
        assertThat(result.getRoleCode()).isEqualTo("PROJECT_MANAGER");
    }

    /**
     * TC15 - Loại bỏ khoảng trắng đầu và cuối role name.
     * Input: name = "  Project Manager  ".
     * Expected: entity và DTO lưu name = "Project Manager".
     */
    @Test
    void createRole_withPaddedNameTrimsRoleName() {
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000016");
        RoleRequestDTO request = createRequest(
                "PROJECT_MANAGER",
                "  Project Manager  ",
                "Manage projects"
        );
        stubSuccessfulCreate("PROJECT_MANAGER", savedId);

        RoleResponseDTO result = roleService.createRole(request);

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRoleName()).isEqualTo("Project Manager");
        assertThat(result.getRoleName()).isEqualTo("Project Manager");
    }

    /**
     * TC16 - Loại bỏ khoảng trắng đầu và cuối description.
     * Input: description = "  Manage projects  ".
     * Expected: entity và DTO lưu description = "Manage projects".
     */
    @Test
    void createRole_withPaddedDescriptionTrimsDescription() {
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000017");
        RoleRequestDTO request = createRequest(
                "PROJECT_MANAGER",
                "Project Manager",
                "  Manage projects  "
        );
        stubSuccessfulCreate("PROJECT_MANAGER", savedId);

        RoleResponseDTO result = roleService.createRole(request);

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRoleDescription()).isEqualTo("Manage projects");
        assertThat(result.getRoleDescription()).isEqualTo("Manage projects");
    }

    /**
     * TC17 - Tạo Role khi description là null.
     * Input: description = null.
     * Expected: tạo thành công và description trong entity, DTO đều là null.
     */
    @Test
    void createRole_withNullDescriptionKeepsDescriptionNull() {
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000018");
        RoleRequestDTO request = createRequest("EMPLOYEE", "Employee", null);
        stubSuccessfulCreate("EMPLOYEE", savedId);

        RoleResponseDTO result = roleService.createRole(request);

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRoleDescription()).isNull();
        assertThat(result.getRoleDescription()).isNull();
    }

    /**
     * TC18 - Tạo Role khi description chỉ chứa khoảng trắng.
     * Input: description = "   ".
     * Expected: chuẩn hóa description thành null trước khi lưu và trả DTO.
     */
    @Test
    void createRole_withBlankDescriptionNormalizesDescriptionToNull() {
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000019");
        RoleRequestDTO request = createRequest("EMPLOYEE", "Employee", "   ");
        stubSuccessfulCreate("EMPLOYEE", savedId);

        RoleResponseDTO result = roleService.createRole(request);

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRoleDescription()).isNull();
        assertThat(result.getRoleDescription()).isNull();
    }

    /**
     * TC19 - Không cho phép tạo Role có code đã tồn tại.
     * Input: code = "admin"; repository xác nhận code "ADMIN" đã tồn tại.
     * Expected: ném lỗi "Role code is already in use!" và không gọi save.
     */
    @Test
    void createRole_whenCodeAlreadyExistsThrowsException() {
        RoleRequestDTO request = createRequest(
                "admin",
                "Another Administrator",
                "Duplicate role"
        );
        when(roleRepository.existsByRoleCodeIgnoreCase("ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> roleService.createRole(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Role code is already in use!");
        verify(roleRepository).existsByRoleCodeIgnoreCase("ADMIN");
        verify(roleRepository, never()).save(any(Role.class));
    }

    /**
     * TC20 - Không cho phép request tạo Role là null.
     * Input: request = null.
     * Expected: ném lỗi "Role request is required" và repository không được gọi.
     */
    @Test
    void createRole_withNullRequestThrowsException() {
        assertCreateValidationFailure(null, "Role request is required");
    }

    /**
     * TC21 - Không cho phép role code là null.
     * Input: code = null.
     * Expected: ném lỗi quy tắc code và repository không được gọi.
     */
    @Test
    void createRole_withNullCodeThrowsException() {
        RoleRequestDTO request = createRequest(null, "Manager", "Manage team");

        assertCreateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * TC22 - Không cho phép role code rỗng.
     * Input: code = "   ".
     * Expected: ném lỗi quy tắc code và repository không được gọi.
     */
    @Test
    void createRole_withBlankCodeThrowsException() {
        RoleRequestDTO request = createRequest("   ", "Manager", "Manage team");

        assertCreateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * TC23 - Không cho phép role code ngắn hơn hai ký tự.
     * Input: code = "A".
     * Expected: ném lỗi quy tắc code và repository không được gọi.
     */
    @Test
    void createRole_withOneCharacterCodeThrowsException() {
        RoleRequestDTO request = createRequest("A", "Manager", "Manage team");

        assertCreateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * TC24 - Role code phải bắt đầu bằng chữ cái.
     * Input: code = "1ADMIN".
     * Expected: ném lỗi quy tắc code và repository không được gọi.
     */
    @Test
    void createRole_withCodeStartingWithNumberThrowsException() {
        RoleRequestDTO request = createRequest("1ADMIN", "Manager", "Manage team");

        assertCreateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * TC25 - Role code không được chứa ký tự đặc biệt ngoài dấu gạch dưới.
     * Input: code = "PROJECT-MANAGER".
     * Expected: ném lỗi quy tắc code và repository không được gọi.
     */
    @Test
    void createRole_withCodeContainingHyphenThrowsException() {
        RoleRequestDTO request = createRequest(
                "PROJECT-MANAGER",
                "Project Manager",
                "Manage projects"
        );

        assertCreateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * TC26 - Chấp nhận role code tại biên nhỏ nhất là hai ký tự.
     * Input: code = "A1".
     * Expected: tạo thành công và DTO có code "A1".
     */
    @Test
    void createRole_withTwoCharacterCodeSucceeds() {
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000020");
        RoleRequestDTO request = createRequest("A1", "Short Code Role", null);
        stubSuccessfulCreate("A1", savedId);

        RoleResponseDTO result = roleService.createRole(request);

        assertThat(result.getRoleCode()).isEqualTo("A1");
        verify(roleRepository).existsByRoleCodeIgnoreCase("A1");
        verify(roleRepository).save(any(Role.class));
    }

    /**
     * TC27 - Chấp nhận role code tại biên tối đa 50 ký tự.
     * Input: code gồm 50 ký tự hợp lệ.
     * Expected: tạo thành công và DTO giữ đủ code 50 ký tự.
     */
    @Test
    void createRole_withFiftyCharacterCodeSucceeds() {
        String roleCode = "A" + "B".repeat(49);
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000021");
        RoleRequestDTO request = createRequest(roleCode, "Maximum Code Role", null);
        stubSuccessfulCreate(roleCode, savedId);

        RoleResponseDTO result = roleService.createRole(request);

        assertThat(result.getRoleCode()).hasSize(50).isEqualTo(roleCode);
        verify(roleRepository).existsByRoleCodeIgnoreCase(roleCode);
        verify(roleRepository).save(any(Role.class));
    }

    /**
     * TC28 - Không cho phép role code vượt quá 50 ký tự.
     * Input: code gồm 51 ký tự.
     * Expected: ném lỗi quy tắc code và repository không được gọi.
     */
    @Test
    void createRole_withFiftyOneCharacterCodeThrowsException() {
        String roleCode = "A" + "B".repeat(50);
        RoleRequestDTO request = createRequest(roleCode, "Too Long Code Role", null);

        assertCreateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * TC29 - Không cho phép role name là null.
     * Input: name = null.
     * Expected: ném lỗi "Role name is required" và repository không được gọi.
     */
    @Test
    void createRole_withNullNameThrowsException() {
        RoleRequestDTO request = createRequest("MANAGER", null, "Manage team");

        assertCreateValidationFailure(request, "Role name is required");
    }

    /**
     * TC30 - Không cho phép role name chỉ chứa khoảng trắng.
     * Input: name = "   ".
     * Expected: ném lỗi "Role name is required" và repository không được gọi.
     */
    @Test
    void createRole_withBlankNameThrowsException() {
        RoleRequestDTO request = createRequest("MANAGER", "   ", "Manage team");

        assertCreateValidationFailure(request, "Role name is required");
    }

    /**
     * TC31 - Chấp nhận role name tại biên tối đa 100 ký tự.
     * Input: name gồm 100 ký tự.
     * Expected: tạo thành công và DTO giữ đủ name 100 ký tự.
     */
    @Test
    void createRole_withOneHundredCharacterNameSucceeds() {
        String roleName = "N".repeat(100);
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000022");
        RoleRequestDTO request = createRequest("MANAGER", roleName, null);
        stubSuccessfulCreate("MANAGER", savedId);

        RoleResponseDTO result = roleService.createRole(request);

        assertThat(result.getRoleName()).hasSize(100).isEqualTo(roleName);
        verify(roleRepository).save(any(Role.class));
    }

    /**
     * TC32 - Không cho phép role name vượt quá 100 ký tự.
     * Input: name gồm 101 ký tự.
     * Expected: ném lỗi "Role name cannot exceed 100 characters".
     */
    @Test
    void createRole_withOneHundredOneCharacterNameThrowsException() {
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
     * TC33 - Chấp nhận description tại biên tối đa 255 ký tự.
     * Input: description gồm 255 ký tự.
     * Expected: tạo thành công và DTO giữ đủ description 255 ký tự.
     */
    @Test
    void createRole_withTwoHundredFiftyFiveCharacterDescriptionSucceeds() {
        String description = "D".repeat(255);
        UUID savedId = UUID.fromString("00000000-0000-0000-0000-000000000023");
        RoleRequestDTO request = createRequest("MANAGER", "Manager", description);
        stubSuccessfulCreate("MANAGER", savedId);

        RoleResponseDTO result = roleService.createRole(request);

        assertThat(result.getRoleDescription()).hasSize(255).isEqualTo(description);
        verify(roleRepository).save(any(Role.class));
    }

    /**
     * TC34 - Không cho phép description vượt quá 255 ký tự.
     * Input: description gồm 256 ký tự.
     * Expected: ném lỗi "Role description cannot exceed 255 characters".
     */
    @Test
    void createRole_withTwoHundredFiftySixCharacterDescriptionThrowsException() {
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
     * TC35 - Repository phát sinh lỗi khi lưu Role hợp lệ.
     * Input: request ADMIN hợp lệ; repository ném RuntimeException "Database unavailable" khi save.
     * Expected: service truyền nguyên exception ra ngoài.
     */
    @Test
    void createRole_whenRepositorySaveFailsPropagatesException() {
        RoleRequestDTO request = createRequest(
                "ADMIN",
                "Administrator",
                "Manage users and roles"
        );
        RuntimeException repositoryException =
                new RuntimeException("Database unavailable");
        when(roleRepository.existsByRoleCodeIgnoreCase("ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenThrow(repositoryException);

        assertThatThrownBy(() -> roleService.createRole(request))
                .isSameAs(repositoryException)
                .hasMessage("Database unavailable");
        verify(roleRepository).existsByRoleCodeIgnoreCase("ADMIN");
        verify(roleRepository).save(any(Role.class));
    }

    /**
     * TC36 - Cập nhật Role thành công với dữ liệu hợp lệ.
     * Input: Role ADMIN tồn tại; cập nhật name và description nhưng giữ nguyên code.
     * Expected: giữ id, code, createdAt; cập nhật dữ liệu, updatedAt và trả DTO đầy đủ.
     */
    @Test
    void updateRole_withValidRequestUpdatesRoleAndReturnsDto() {
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000024");
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

        RoleResponseDTO result = roleService.updateRole(roleId, request);

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
     * TC37 - Chấp nhận code viết thường nếu vẫn là code hiện tại.
     * Input: Role hiện có code "ADMIN"; request code = "admin".
     * Expected: cập nhật thành công, kiểm tra trùng bằng "ADMIN" và giữ code "ADMIN".
     */
    @Test
    void updateRole_withLowercaseCurrentCodeSucceeds() {
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000025");
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("admin", "Administrator", null);
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = roleService.updateRole(roleId, request);

        assertThat(result.getRoleCode()).isEqualTo("ADMIN");
        verify(roleRepository).existsByRoleCodeIgnoreCaseAndIdNot("ADMIN", roleId);
        verify(roleRepository).save(existingRole);
    }

    /**
     * TC38 - Loại bỏ khoảng trắng quanh code trước khi so sánh.
     * Input: Role hiện có code "ADMIN"; request code = "  ADMIN  ".
     * Expected: cập nhật thành công và repository nhận code "ADMIN".
     */
    @Test
    void updateRole_withPaddedCurrentCodeSucceeds() {
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000026");
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("  ADMIN  ", "Administrator", null);
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = roleService.updateRole(roleId, request);

        assertThat(result.getRoleCode()).isEqualTo("ADMIN");
        verify(roleRepository).existsByRoleCodeIgnoreCaseAndIdNot("ADMIN", roleId);
        verify(roleRepository).save(existingRole);
    }

    /**
     * TC39 - Loại bỏ khoảng trắng quanh role name khi cập nhật.
     * Input: name = "  Senior Administrator  ".
     * Expected: entity và DTO có name = "Senior Administrator".
     */
    @Test
    void updateRole_withPaddedNameTrimsRoleName() {
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000027");
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest(
                "ADMIN",
                "  Senior Administrator  ",
                null
        );
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = roleService.updateRole(roleId, request);

        assertThat(existingRole.getRoleName()).isEqualTo("Senior Administrator");
        assertThat(result.getRoleName()).isEqualTo("Senior Administrator");
        verify(roleRepository).save(existingRole);
    }

    /**
     * TC40 - Loại bỏ khoảng trắng quanh description khi cập nhật.
     * Input: description = "  Updated description  ".
     * Expected: entity và DTO có description = "Updated description".
     */
    @Test
    void updateRole_withPaddedDescriptionTrimsDescription() {
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000028");
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest(
                "ADMIN",
                "Administrator",
                "  Updated description  "
        );
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = roleService.updateRole(roleId, request);

        assertThat(existingRole.getRoleDescription()).isEqualTo("Updated description");
        assertThat(result.getRoleDescription()).isEqualTo("Updated description");
        verify(roleRepository).save(existingRole);
    }

    /**
     * TC41 - Cho phép cập nhật description thành null.
     * Input: description = null.
     * Expected: cập nhật thành công và description trong entity, DTO đều là null.
     */
    @Test
    void updateRole_withNullDescriptionKeepsDescriptionNull() {
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000029");
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("ADMIN", "Administrator", null);
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = roleService.updateRole(roleId, request);

        assertThat(existingRole.getRoleDescription()).isNull();
        assertThat(result.getRoleDescription()).isNull();
        verify(roleRepository).save(existingRole);
    }

    /**
     * TC42 - Chuẩn hóa description chỉ chứa khoảng trắng thành null.
     * Input: description = "   ".
     * Expected: cập nhật thành công và description được lưu là null.
     */
    @Test
    void updateRole_withBlankDescriptionNormalizesDescriptionToNull() {
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000030");
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("ADMIN", "Administrator", "   ");
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = roleService.updateRole(roleId, request);

        assertThat(existingRole.getRoleDescription()).isNull();
        assertThat(result.getRoleDescription()).isNull();
        verify(roleRepository).save(existingRole);
    }

    /**
     * TC43 - Chấp nhận code hiện tại ở biên tối thiểu hai ký tự.
     * Input: Role và request cùng có code = "A1".
     * Expected: cập nhật thành công và DTO giữ code "A1".
     */
    @Test
    void updateRole_withTwoCharacterCurrentCodeSucceeds() {
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000031");
        Role existingRole = existingRole(roleId, "A1");
        RoleRequestDTO request = createRequest("A1", "Short Code Role", null);
        stubSuccessfulUpdate(roleId, existingRole, "A1");

        RoleResponseDTO result = roleService.updateRole(roleId, request);

        assertThat(result.getRoleCode()).isEqualTo("A1");
        verify(roleRepository).save(existingRole);
    }

    /**
     * TC44 - Chấp nhận code hiện tại ở biên tối đa 50 ký tự.
     * Input: Role và request cùng có code hợp lệ dài 50 ký tự.
     * Expected: cập nhật thành công và DTO giữ đủ code 50 ký tự.
     */
    @Test
    void updateRole_withFiftyCharacterCurrentCodeSucceeds() {
        String roleCode = "A" + "B".repeat(49);
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000032");
        Role existingRole = existingRole(roleId, roleCode);
        RoleRequestDTO request = createRequest(roleCode, "Maximum Code Role", null);
        stubSuccessfulUpdate(roleId, existingRole, roleCode);

        RoleResponseDTO result = roleService.updateRole(roleId, request);

        assertThat(result.getRoleCode()).hasSize(50).isEqualTo(roleCode);
        verify(roleRepository).save(existingRole);
    }

    /**
     * TC45 - Chấp nhận role name ở biên tối đa 100 ký tự.
     * Input: name gồm 100 ký tự.
     * Expected: cập nhật thành công và DTO giữ đủ name 100 ký tự.
     */
    @Test
    void updateRole_withOneHundredCharacterNameSucceeds() {
        String roleName = "N".repeat(100);
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000033");
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("ADMIN", roleName, null);
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = roleService.updateRole(roleId, request);

        assertThat(result.getRoleName()).hasSize(100).isEqualTo(roleName);
        verify(roleRepository).save(existingRole);
    }

    /**
     * TC46 - Chấp nhận description ở biên tối đa 255 ký tự.
     * Input: description gồm 255 ký tự.
     * Expected: cập nhật thành công và DTO giữ đủ description 255 ký tự.
     */
    @Test
    void updateRole_withTwoHundredFiftyFiveCharacterDescriptionSucceeds() {
        String description = "D".repeat(255);
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000034");
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("ADMIN", "Administrator", description);
        stubSuccessfulUpdate(roleId, existingRole, "ADMIN");

        RoleResponseDTO result = roleService.updateRole(roleId, request);

        assertThat(result.getRoleDescription()).hasSize(255).isEqualTo(description);
        verify(roleRepository).save(existingRole);
    }

    /**
     * TC47 - Không cho phép request cập nhật là null.
     * Input: request = null.
     * Expected: ném "Role request is required" và repository không được gọi.
     */
    @Test
    void updateRole_withNullRequestThrowsException() {
        assertUpdateValidationFailure(null, "Role request is required");
    }

    /**
     * TC48 - Không cho phép role code là null khi cập nhật.
     * Input: code = null.
     * Expected: ném lỗi quy tắc code và repository không được gọi.
     */
    @Test
    void updateRole_withNullCodeThrowsException() {
        RoleRequestDTO request = createRequest(null, "Administrator", null);

        assertUpdateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * TC49 - Không cho phép role code rỗng khi cập nhật.
     * Input: code = "   ".
     * Expected: ném lỗi quy tắc code và repository không được gọi.
     */
    @Test
    void updateRole_withBlankCodeThrowsException() {
        RoleRequestDTO request = createRequest("   ", "Administrator", null);

        assertUpdateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * TC50 - Không cho phép role code ngắn hơn hai ký tự khi cập nhật.
     * Input: code = "A".
     * Expected: ném lỗi quy tắc code và repository không được gọi.
     */
    @Test
    void updateRole_withOneCharacterCodeThrowsException() {
        RoleRequestDTO request = createRequest("A", "Administrator", null);

        assertUpdateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * TC51 - Role code cập nhật phải bắt đầu bằng chữ cái.
     * Input: code = "1ADMIN".
     * Expected: ném lỗi quy tắc code và repository không được gọi.
     */
    @Test
    void updateRole_withCodeStartingWithNumberThrowsException() {
        RoleRequestDTO request = createRequest("1ADMIN", "Administrator", null);

        assertUpdateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * TC52 - Role code cập nhật không được chứa dấu gạch ngang.
     * Input: code = "PROJECT-MANAGER".
     * Expected: ném lỗi quy tắc code và repository không được gọi.
     */
    @Test
    void updateRole_withCodeContainingHyphenThrowsException() {
        RoleRequestDTO request = createRequest(
                "PROJECT-MANAGER",
                "Project Manager",
                null
        );

        assertUpdateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * TC53 - Không cho phép role code cập nhật vượt quá 50 ký tự.
     * Input: code gồm 51 ký tự.
     * Expected: ném lỗi quy tắc code và repository không được gọi.
     */
    @Test
    void updateRole_withFiftyOneCharacterCodeThrowsException() {
        RoleRequestDTO request = createRequest(
                "A" + "B".repeat(50),
                "Administrator",
                null
        );

        assertUpdateValidationFailure(request, invalidRoleCodeMessage());
    }

    /**
     * TC54 - Không cho phép role name là null khi cập nhật.
     * Input: name = null.
     * Expected: ném "Role name is required" và repository không được gọi.
     */
    @Test
    void updateRole_withNullNameThrowsException() {
        RoleRequestDTO request = createRequest("ADMIN", null, null);

        assertUpdateValidationFailure(request, "Role name is required");
    }

    /**
     * TC55 - Không cho phép role name rỗng khi cập nhật.
     * Input: name = "   ".
     * Expected: ném "Role name is required" và repository không được gọi.
     */
    @Test
    void updateRole_withBlankNameThrowsException() {
        RoleRequestDTO request = createRequest("ADMIN", "   ", null);

        assertUpdateValidationFailure(request, "Role name is required");
    }

    /**
     * TC56 - Không cho phép role name vượt quá 100 ký tự khi cập nhật.
     * Input: name gồm 101 ký tự.
     * Expected: ném "Role name cannot exceed 100 characters".
     */
    @Test
    void updateRole_withOneHundredOneCharacterNameThrowsException() {
        RoleRequestDTO request = createRequest("ADMIN", "N".repeat(101), null);

        assertUpdateValidationFailure(
                request,
                "Role name cannot exceed 100 characters"
        );
    }

    /**
     * TC57 - Không cho phép description vượt quá 255 ký tự khi cập nhật.
     * Input: description gồm 256 ký tự.
     * Expected: ném "Role description cannot exceed 255 characters".
     */
    @Test
    void updateRole_withTwoHundredFiftySixCharacterDescriptionThrowsException() {
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
     * TC58 - Cập nhật Role không tồn tại.
     * Input: id không có trong repository và request hợp lệ.
     * Expected: ném lỗi chứa id, không kiểm tra trùng code và không gọi save.
     */
    @Test
    void updateRole_whenRoleDoesNotExistThrowsException() {
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000035");
        RoleRequestDTO request = createRequest("ADMIN", "Administrator", null);
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.updateRole(roleId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Role not found with id: " + roleId);
        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).existsByRoleCodeIgnoreCaseAndIdNot(
                any(String.class),
                any(UUID.class)
        );
        verify(roleRepository, never()).save(any(Role.class));
    }

    /**
     * TC59 - Không cho phép thay đổi role code sau khi tạo.
     * Input: Role hiện có code "ADMIN" nhưng request code = "MANAGER".
     * Expected: ném "Role code cannot be changed after creation!" và không gọi save.
     */
    @Test
    void updateRole_whenCodeIsChangedThrowsException() {
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000036");
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("MANAGER", "Administrator", null);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));

        assertThatThrownBy(() -> roleService.updateRole(roleId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Role code cannot be changed after creation!");
        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).existsByRoleCodeIgnoreCaseAndIdNot(
                any(String.class),
                any(UUID.class)
        );
        verify(roleRepository, never()).save(any(Role.class));
    }

    /**
     * TC60 - Không cho phép code bị một Role khác sử dụng.
     * Input: Role ADMIN tồn tại; repository báo code ADMIN thuộc id khác.
     * Expected: ném lỗi trùng code và không gọi save.
     */
    @Test
    void updateRole_whenCodeBelongsToAnotherRoleThrowsException() {
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000037");
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("admin", "Administrator", null);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByRoleCodeIgnoreCaseAndIdNot("ADMIN", roleId))
                .thenReturn(true);

        assertThatThrownBy(() -> roleService.updateRole(roleId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Role code is already in use by another role!");
        verify(roleRepository).findById(roleId);
        verify(roleRepository).existsByRoleCodeIgnoreCaseAndIdNot("ADMIN", roleId);
        verify(roleRepository, never()).save(any(Role.class));
    }

    /**
     * TC61 - Repository phát sinh lỗi khi tìm Role cần cập nhật.
     * Input: id hợp lệ; findById ném RuntimeException "Database unavailable".
     * Expected: service truyền nguyên exception ra ngoài và không gọi save.
     */
    @Test
    void updateRole_whenRepositoryFindFailsPropagatesException() {
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000038");
        RoleRequestDTO request = createRequest("ADMIN", "Administrator", null);
        RuntimeException repositoryException =
                new RuntimeException("Database unavailable");
        when(roleRepository.findById(roleId)).thenThrow(repositoryException);

        assertThatThrownBy(() -> roleService.updateRole(roleId, request))
                .isSameAs(repositoryException)
                .hasMessage("Database unavailable");
        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).save(any(Role.class));
    }

    /**
     * TC62 - Repository phát sinh lỗi khi lưu Role đã cập nhật.
     * Input: Role và request hợp lệ; save ném RuntimeException "Database unavailable".
     * Expected: service truyền nguyên exception ra ngoài.
     */
    @Test
    void updateRole_whenRepositorySaveFailsPropagatesException() {
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000039");
        Role existingRole = existingRole(roleId, "ADMIN");
        RoleRequestDTO request = createRequest("ADMIN", "Administrator", null);
        RuntimeException repositoryException =
                new RuntimeException("Database unavailable");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByRoleCodeIgnoreCaseAndIdNot("ADMIN", roleId))
                .thenReturn(false);
        when(roleRepository.save(existingRole)).thenThrow(repositoryException);

        assertThatThrownBy(() -> roleService.updateRole(roleId, request))
                .isSameAs(repositoryException)
                .hasMessage("Database unavailable");
        verify(roleRepository).findById(roleId);
        verify(roleRepository).existsByRoleCodeIgnoreCaseAndIdNot("ADMIN", roleId);
        verify(roleRepository).save(existingRole);
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
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        assertThatThrownBy(() -> roleService.updateRole(roleId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(expectedMessage);
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
        assertThatThrownBy(() -> roleService.createRole(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(expectedMessage);
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
