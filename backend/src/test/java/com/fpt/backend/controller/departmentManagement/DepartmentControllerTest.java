package com.fpt.backend.controller.departmentManagement;

import com.fpt.backend.controller.departmentController.DepartmentController;
import com.fpt.backend.dto.request.department.DepartmentRequestDTO;
import com.fpt.backend.dto.response.department.DepartmentResponseDTO;
import com.fpt.backend.entity.Company;
import com.fpt.backend.entity.Departments;
import com.fpt.backend.repository.department.DepartmentRepository;
import com.fpt.backend.service.impl.department.DepartmentServiceImpl;
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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit test gộp DepartmentController và DepartmentServiceImpl thật.
 * Chỉ mock DepartmentRepository, không chạy Spring, HTTP, Security hoặc database.
 * Mỗi UTCID đối chiếu với một cột trong báo cáo Department Management.
 * Kết quả tìm kiếm do mock chuẩn bị: các test không chứng minh JPQL lọc/sắp xếp đúng.
 * Không tự áp dụng giới hạn mã Role cho Department. Những quy tắc chưa có trong
 * source (độ dài tối đa, tập giá trị status) được ghi riêng trong báo cáo.
 */
@Tag("department-management")
@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {
    private static final UUID DEPARTMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID SECOND_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");
    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 8, 10, 9, 30);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 8, 11, 10, 45);

    @Mock
    private DepartmentRepository departmentRepository;
    @InjectMocks
    private DepartmentServiceImpl departmentService;
    private DepartmentController departmentController;

    @BeforeEach
    void setUp() {
        departmentController = new DepartmentController();
        ReflectionTestUtils.setField(departmentController, "departmentService", departmentService);
    }

    // List Department - UTCID01
    // Input search="HR", status="". Tìm bằng "hr", trả đúng phòng HR và HTTP 200.
    @Test
    @DisplayName("List Department - UTCID01")
    void search_withUppercaseKeywordUsesLowercase() {
        assertSearch("HR", "", "hr", "");
    }

    // List Department - UTCID02
    // Input search="hr", status="". Giữ từ khóa chữ thường, trả đúng phòng HR.
    @Test
    @DisplayName("List Department - UTCID02")
    void search_withLowercaseKeywordKeepsLowercase() {
        assertSearch("hr", "", "hr", "");
    }

    // List Department - UTCID03
    // Input search="hR", status="". Chuẩn hóa từ khóa thành "hr".
    @Test
    @DisplayName("List Department - UTCID03")
    void search_withMixedCaseKeywordUsesLowercase() {
        assertSearch("hR", "", "hr", "");
    }

    // List Department - UTCID04
    // Input search="  HR  ", status="". Bỏ khoảng trắng hai đầu, tìm bằng "hr".
    @Test
    @DisplayName("List Department - UTCID04")
    void search_trimsKeyword() {
        assertSearch("  HR  ", "", "hr", "");
    }

    // List Department - UTCID05
    // Input search="", status="". Không áp dụng từ khóa hoặc bộ lọc trạng thái.
    @Test
    @DisplayName("List Department - UTCID05")
    void search_withEmptyKeywordUsesEmptyFilter() {
        assertSearch("", "", "", "");
    }

    // List Department - UTCID06
    // Input search="   ", status="". Từ khóa toàn khoảng trắng trở thành "".
    @Test
    @DisplayName("List Department - UTCID06")
    void search_withBlankKeywordUsesEmptyFilter() {
        assertSearch("   ", "", "", "");
    }

    // List Department - UTCID07
    // Input search=null, status="Active". Không lọc từ khóa, status thành "active".
    @Test
    @DisplayName("List Department - UTCID07")
    void search_withNullKeywordUsesEmptyFilter() {
        assertSearch(null, "Active", "", "active");
    }

    // List Department - UTCID08
    // Input search=" HÀNH CHÍNH ". Giữ dấu tiếng Việt, tìm bằng "hành chính".
    @Test
    @DisplayName("List Department - UTCID08")
    void search_normalizesVietnameseKeyword() {
        Departments administrative = department(DEPARTMENT_ID, "ADM", "Hành chính", "Active", true);
        assertSearchResult(" HÀNH CHÍNH ", "", "hành chính", "", List.of(administrative));
    }

    // List Department - UTCID09
    // Input status="ACTIVE". Gửi bộ lọc "active" và giữ đúng dữ liệu trả về.
    @Test
    @DisplayName("List Department - UTCID09")
    void search_withUppercaseStatusUsesLowercase() {
        assertSearch("HR", "ACTIVE", "hr", "active");
    }

    // List Department - UTCID10
    // Input status="active". Giữ trạng thái chữ thường khi tìm kiếm.
    @Test
    @DisplayName("List Department - UTCID10")
    void search_withLowercaseStatusKeepsLowercase() {
        assertSearch("HR", "active", "hr", "active");
    }

    // List Department - UTCID11
    // Input status="aCtIvE". Chuẩn hóa thành "active".
    @Test
    @DisplayName("List Department - UTCID11")
    void search_withMixedCaseStatusUsesLowercase() {
        assertSearch("HR", "aCtIvE", "hr", "active");
    }

    // List Department - UTCID12
    // Input status="  Active  ". Bỏ khoảng trắng hai đầu và chuyển chữ thường.
    @Test
    @DisplayName("List Department - UTCID12")
    void search_trimsStatus() {
        assertSearch("HR", "  Active  ", "hr", "active");
    }

    // List Department - UTCID13
    // Input status="INACTIVE". Gửi bộ lọc "inactive", trả phòng Inactive.
    @Test
    @DisplayName("List Department - UTCID13")
    void search_normalizesInactiveStatus() {
        assertSearch("HR", "INACTIVE", "hr", "inactive");
    }

    // List Department - UTCID14
    // Input status=null. Chuyển thành "" để không lọc trạng thái.
    @Test
    @DisplayName("List Department - UTCID14")
    void search_withNullStatusUsesEmptyFilter() {
        assertSearch("HR", null, "hr", "");
    }

    // List Department - UTCID15
    // Input status="   ". Chuyển thành "" để không lọc trạng thái.
    @Test
    @DisplayName("List Department - UTCID15")
    void search_withBlankStatusUsesEmptyFilter() {
        assertSearch("HR", "   ", "hr", "");
    }

    // List Department - UTCID16
    // Input search=" HR ", status=" Inactive ". Chuẩn hóa cả hai bộ lọc cùng lúc.
    @Test
    @DisplayName("List Department - UTCID16")
    void search_combinesKeywordAndStatus() {
        assertSearch(" HR ", " Inactive ", "hr", "inactive");
    }

    // List Department - UTCID17
    // Phòng HR có company và đủ ngày tạo/sửa. Trả đúng toàn bộ 7 trường thông tin.
    @Test
    @DisplayName("List Department - UTCID17")
    void search_mapsEveryResponseField() {
        assertSearchResult("HR", "Active", "hr", "active", List.of(existingDepartment()));
    }

    // List Department - UTCID18
    // Phòng HR chưa có company. Trả companyId=null, các trường khác không mất dữ liệu.
    @Test
    @DisplayName("List Department - UTCID18")
    void search_withNoCompanyReturnsNullCompanyId() {
        Departments department = existingDepartment();
        department.setCompany(null);
        assertSearchResult("HR", "", "hr", "", List.of(department));
    }

    // List Department - UTCID19
    // Phòng HR chưa có ngày cập nhật. Trả updatedAt=null, không tự sinh ngày sửa.
    @Test
    @DisplayName("List Department - UTCID19")
    void search_withNoUpdateDateKeepsNull() {
        Departments department = existingDepartment();
        department.setUpdatedAt(null);
        assertSearchResult("HR", "", "hr", "", List.of(department));
    }

    // List Department - UTCID20
    // Input search="not-found", status="". Không có kết quả: HTTP 200 và danh sách [].
    @Test
    @DisplayName("List Department - UTCID20")
    void search_withNoMatchesReturnsEmptyList() {
        assertSearchResult("not-found", "", "not-found", "", List.of());
    }

    // List Department - UTCID21
    // Có 2 kết quả IT, HR theo thứ tự đã chuẩn bị. Response giữ nguyên thứ tự đó.
    @Test
    @DisplayName("List Department - UTCID21")
    void search_preservesResultOrder() {
        Departments it = department(SECOND_ID, "IT", "Information Technology", "Inactive", false);
        assertSearchResult("", "", "", "", List.of(it, existingDepartment()));
    }

    // List Department - UTCID22
    // Lỗi đọc dữ liệu "Database unavailable": controller ném lại đúng exception ban đầu.
    @Test
    @DisplayName("List Department - UTCID22")
    void search_propagatesReadFailure() {
        RuntimeException failure = new RuntimeException("Database unavailable");
        when(departmentRepository.searchAndFilter("hr", "active")).thenThrow(failure);
        assertThatThrownBy(() -> departmentController.searchDepartments("HR", "Active"))
                .isSameAs(failure).hasMessage("Database unavailable");
        verify(departmentRepository).searchAndFilter("hr", "active");
        verifyNoMoreInteractions(departmentRepository);
    }

    // List Department - UTCID23
    // Input search=null, status=null. Cả hai chuyển thành "", không phát sinh lỗi null.
    @Test
    @DisplayName("List Department - UTCID23")
    void search_withBothNullFiltersUsesEmptyStrings() {
        assertSearch(null, null, "", "");
    }

    // Get All Department - UTCID01
    // Không có tham số; có HR Active và IT Inactive. Lấy cả hai, giữ thứ tự và đủ 7 trường.
    @Test
    @DisplayName("Get All Department - UTCID01")
    void getAll_returnsActiveAndInactiveDepartments() {
        Departments it = department(SECOND_ID, "IT", "Information Technology", "Inactive", true);
        assertGetAll(List.of(existingDepartment(), it));
    }

    // Get All Department - UTCID02
    // Danh sách phòng ban trống. Trả HTTP 200 và [] (không phải null).
    @Test
    @DisplayName("Get All Department - UTCID02")
    void getAll_withNoDepartmentsReturnsEmptyList() {
        assertGetAll(List.of());
    }

    // Get All Department - UTCID03
    // Có phòng HR chưa có company. Vẫn lấy được dữ liệu, companyId=null.
    @Test
    @DisplayName("Get All Department - UTCID03")
    void getAll_withNoCompanyKeepsNullCompanyId() {
        Departments department = existingDepartment();
        department.setCompany(null);
        assertGetAll(List.of(department));
    }

    // Get All Department - UTCID04
    // Lỗi đọc "Unable to fetch departments". Ném lại cùng exception, không trả thành công giả.
    @Test
    @DisplayName("Get All Department - UTCID04")
    void getAll_propagatesReadFailure() {
        RuntimeException failure = new RuntimeException("Unable to fetch departments");
        when(departmentRepository.searchAndFilter("", "")).thenThrow(failure);
        assertThatThrownBy(() -> departmentController.getAllDepartments())
                .isSameAs(failure).hasMessage("Unable to fetch departments");
        verify(departmentRepository).searchAndFilter("", "");
        verifyNoMoreInteractions(departmentRepository);
    }

    // View Department - UTCID01
    // ID ...0101 tồn tại và có company. Trả HTTP 200, đúng toàn bộ 7 trường của phòng HR.
    @Test
    @DisplayName("View Department - UTCID01")
    void view_returnsEveryFieldOfExistingDepartment() {
        assertView(existingDepartment());
    }

    // View Department - UTCID02
    // ID ...0101 không tồn tại. Trả HTTP 404 và lỗi Department not found with id.
    @Test
    @DisplayName("View Department - UTCID02")
    void view_withMissingDepartmentReturnsNotFound() {
        when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.empty());
        assertError(departmentController.getDepartmentById(DEPARTMENT_ID), HttpStatus.NOT_FOUND,
                "Department not found with id: " + DEPARTMENT_ID);
        verify(departmentRepository).findById(DEPARTMENT_ID);
        verifyNoMoreInteractions(departmentRepository);
    }

    // View Department - UTCID03
    // Phòng HR tồn tại nhưng chưa có company. Trả companyId=null, giữ đúng các thông tin khác.
    @Test
    @DisplayName("View Department - UTCID03")
    void view_withNoCompanyReturnsNullCompanyId() {
        Departments department = existingDepartment();
        department.setCompany(null);
        assertView(department);
    }

    // View Department - UTCID04
    // Phòng HR chưa có ngày cập nhật. Trả updatedAt=null, không thay đổi dữ liệu.
    @Test
    @DisplayName("View Department - UTCID04")
    void view_withNoUpdateDateKeepsNull() {
        Departments department = existingDepartment();
        department.setUpdatedAt(null);
        assertView(department);
    }

    // View Department - UTCID05
    // Lỗi đọc "Database unavailable". Controller hiện trả HTTP 404 kèm đúng lỗi.
    @Test
    @DisplayName("View Department - UTCID05")
    void view_whenReadFailsReturnsNotFoundResponse() {
        when(departmentRepository.findById(DEPARTMENT_ID))
                .thenThrow(new RuntimeException("Database unavailable"));
        assertError(departmentController.getDepartmentById(DEPARTMENT_ID), HttpStatus.NOT_FOUND,
                "Database unavailable");
        verify(departmentRepository).findById(DEPARTMENT_ID);
        verifyNoMoreInteractions(departmentRepository);
    }

    // Create Department - UTCID01
    // Tạo Human Resources/HR/Active. HTTP 201, lưu đúng dữ liệu, ngày tạo mới, company/ngày sửa null.
    @Test
    @DisplayName("Create Department - UTCID01")
    void create_withValidRequestSavesAndReturnsDepartment() {
        assertCreate(request("Human Resources", "HR", "Active"), "Human Resources", "HR", "Active");
    }

    // Create Department - UTCID02
    // Input status="Inactive". Lưu và trả đúng Inactive, không đổi thành Active.
    @Test
    @DisplayName("Create Department - UTCID02")
    void create_withInactiveStatusKeepsInactive() {
        assertCreate(request("Human Resources", "HR", "Inactive"), "Human Resources", "HR", "Inactive");
    }

    // Create Department - UTCID03
    // Input code="hr". Kiểm tra trùng, lưu và trả về mã viết hoa HR.
    @Test
    @DisplayName("Create Department - UTCID03")
    void create_withLowercaseCodeUsesUppercase() {
        assertCreate(request("Human Resources", "hr", "Active"), "Human Resources", "HR", "Active");
    }

    // Create Department - UTCID04
    // Input code="  HR  ". Bỏ khoảng trắng hai đầu trước kiểm tra trùng và lưu.
    @Test
    @DisplayName("Create Department - UTCID04")
    void create_trimsCode() {
        assertCreate(request("Human Resources", "  HR  ", "Active"), "Human Resources", "HR", "Active");
    }

    // Create Department - UTCID05
    // Input code="  hR  ". Kết hợp trim và viết hoa thành HR trước khi kiểm tra trùng.
    @Test
    @DisplayName("Create Department - UTCID05")
    void create_trimsAndUppercasesCode() {
        assertCreate(request("Human Resources", "  hR  ", "Active"), "Human Resources", "HR", "Active");
    }

    // Create Department - UTCID06
    // Input name="  Human Resources  ". Bỏ khoảng trắng hai đầu, giữ khoảng trắng giữa từ.
    @Test
    @DisplayName("Create Department - UTCID06")
    void create_trimsName() {
        assertCreate(request("  Human Resources  ", "HR", "Active"), "Human Resources", "HR", "Active");
    }

    // Create Department - UTCID07
    // Input name="  Phòng Hành chính  ", code="ADM". Giữ nguyên dấu tiếng Việt sau trim.
    @Test
    @DisplayName("Create Department - UTCID07")
    void create_preservesVietnameseName() {
        assertCreate(request("  Phòng Hành chính  ", "ADM", "Active"), "Phòng Hành chính", "ADM", "Active");
    }

    // Create Department - UTCID08
    // Input status=null. Dùng trạng thái mặc định Active.
    @Test
    @DisplayName("Create Department - UTCID08")
    void create_withNullStatusDefaultsToActive() {
        assertCreate(request("Human Resources", "HR", null), "Human Resources", "HR", "Active");
    }

    // Create Department - UTCID09
    // Input status="". Dùng trạng thái mặc định Active.
    @Test
    @DisplayName("Create Department - UTCID09")
    void create_withEmptyStatusDefaultsToActive() {
        assertCreate(request("Human Resources", "HR", ""), "Human Resources", "HR", "Active");
    }

    // Create Department - UTCID10
    // Input status="   ". Dùng Active thay vì lưu chuỗi khoảng trắng.
    @Test
    @DisplayName("Create Department - UTCID10")
    void create_withBlankStatusDefaultsToActive() {
        assertCreate(request("Human Resources", "HR", "   "), "Human Resources", "HR", "Active");
    }

    // Create Department - UTCID11
    // Input status chỉ có tab/xuống dòng. Dùng Active.
    @Test
    @DisplayName("Create Department - UTCID11")
    void create_withWhitespaceControlStatusDefaultsToActive() {
        assertCreate(request("Human Resources", "HR", "\t\n"), "Human Resources", "HR", "Active");
    }

    // Create Department - UTCID12
    // Input request=null. HTTP 400, Department request is required, không truy cập repository.
    @Test
    @DisplayName("Create Department - UTCID12")
    void create_withNullRequestIsRejected() {
        assertCreateValidation(null, "Department request is required");
    }

    // Create Department - UTCID13
    // Input name=null. HTTP 400, Department name is required, không gọi lưu.
    @Test
    @DisplayName("Create Department - UTCID13")
    void create_withNullNameIsRejected() {
        assertCreateValidation(request(null, "HR", "Active"), "Department name is required");
    }

    // Create Department - UTCID14
    // Input name="". HTTP 400 và lỗi thiếu tên phòng ban.
    @Test
    @DisplayName("Create Department - UTCID14")
    void create_withEmptyNameIsRejected() {
        assertCreateValidation(request("", "HR", "Active"), "Department name is required");
    }

    // Create Department - UTCID15
    // Input name="   ". HTTP 400, không lưu tên chỉ có khoảng trắng.
    @Test
    @DisplayName("Create Department - UTCID15")
    void create_withBlankNameIsRejected() {
        assertCreateValidation(request("   ", "HR", "Active"), "Department name is required");
    }

    // Create Department - UTCID16
    // Input name chỉ gồm tab/xuống dòng. HTTP 400 và lỗi thiếu tên.
    @Test
    @DisplayName("Create Department - UTCID16")
    void create_withWhitespaceControlNameIsRejected() {
        assertCreateValidation(request("\t\n", "HR", "Active"), "Department name is required");
    }

    // Create Department - UTCID17
    // Input name là khoảng trắng Unicode U+2003. isBlank phải chặn, không gọi lưu.
    @Test
    @DisplayName("Create Department - UTCID17")
    void create_withUnicodeBlankNameIsRejected() {
        assertCreateValidation(request("\u2003", "HR", "Active"), "Department name is required");
    }

    // Create Department - UTCID18
    // Input code=null. HTTP 400, Department code is required.
    @Test
    @DisplayName("Create Department - UTCID18")
    void create_withNullCodeIsRejected() {
        assertCreateValidation(request("Human Resources", null, "Active"), "Department code is required");
    }

    // Create Department - UTCID19
    // Input code="". HTTP 400 và lỗi thiếu mã phòng ban.
    @Test
    @DisplayName("Create Department - UTCID19")
    void create_withEmptyCodeIsRejected() {
        assertCreateValidation(request("Human Resources", "", "Active"), "Department code is required");
    }

    // Create Department - UTCID20
    // Input code="   ". HTTP 400, không kiểm tra trùng hoặc lưu mã rỗng.
    @Test
    @DisplayName("Create Department - UTCID20")
    void create_withBlankCodeIsRejected() {
        assertCreateValidation(request("Human Resources", "   ", "Active"), "Department code is required");
    }

    // Create Department - UTCID21
    // Input code chỉ có tab/xuống dòng. HTTP 400 và lỗi thiếu mã.
    @Test
    @DisplayName("Create Department - UTCID21")
    void create_withWhitespaceControlCodeIsRejected() {
        assertCreateValidation(request("Human Resources", "\t\n", "Active"), "Department code is required");
    }

    // Create Department - UTCID22
    // Input code là khoảng trắng Unicode U+2003. HTTP 400, không gọi lưu.
    @Test
    @DisplayName("Create Department - UTCID22")
    void create_withUnicodeBlankCodeIsRejected() {
        assertCreateValidation(request("Human Resources", "\u2003", "Active"), "Department code is required");
    }

    // Create Department - UTCID23
    // Đã có mã HR, input code="HR". HTTP 400 lỗi trùng mã, không gọi lưu.
    @Test
    @DisplayName("Create Department - UTCID23")
    void create_withDuplicateUppercaseCodeIsRejected() {
        assertCreateDuplicate("HR");
    }

    // Create Department - UTCID24
    // Đã có mã HR, input code="hr". Vẫn phát hiện trùng mã sau viết hoa.
    @Test
    @DisplayName("Create Department - UTCID24")
    void create_withDuplicateLowercaseCodeIsRejected() {
        assertCreateDuplicate("hr");
    }

    // Create Department - UTCID25
    // Đã có mã HR, input code="  hR  ". Vẫn phát hiện trùng sau trim/viết hoa.
    @Test
    @DisplayName("Create Department - UTCID25")
    void create_withDuplicatePaddedCodeIsRejected() {
        assertCreateDuplicate("  hR  ");
    }

    // Create Department - UTCID26
    // Lỗi kiểm tra mã trùng. HTTP 400, giữ đúng lỗi và không tiếp tục lưu.
    @Test
    @DisplayName("Create Department - UTCID26")
    void create_whenDuplicateCheckFailsDoesNotSave() {
        when(departmentRepository.existsByDepartmentCodeIgnoreCase("HR"))
                .thenThrow(new RuntimeException("Unable to check department code"));
        assertError(departmentController.createDepartment(request("Human Resources", "HR", "Active")),
                HttpStatus.BAD_REQUEST, "Unable to check department code");
        verify(departmentRepository).existsByDepartmentCodeIgnoreCase("HR");
        verify(departmentRepository, never()).save(any(Departments.class));
        verifyNoMoreInteractions(departmentRepository);
    }

    // Create Department - UTCID27
    // Dữ liệu hợp lệ nhưng lưu bị lỗi. HTTP 400, message Database unavailable.
    @Test
    @DisplayName("Create Department - UTCID27")
    void create_whenSaveFailsReturnsBadRequest() {
        when(departmentRepository.existsByDepartmentCodeIgnoreCase("HR")).thenReturn(false);
        when(departmentRepository.save(any(Departments.class)))
                .thenThrow(new RuntimeException("Database unavailable"));
        assertError(departmentController.createDepartment(request("Human Resources", "HR", "Active")),
                HttpStatus.BAD_REQUEST, "Database unavailable");
        InOrder calls = inOrder(departmentRepository);
        calls.verify(departmentRepository).existsByDepartmentCodeIgnoreCase("HR");
        calls.verify(departmentRepository).save(any(Departments.class));
        verifyNoMoreInteractions(departmentRepository);
    }

    // Create Department - UTCID28
    // Input name="H" (1 ký tự). Tên không rỗng được chấp nhận, lưu/trả đúng H.
    @Test
    @DisplayName("Create Department - UTCID28")
    void create_withOneCharacterNameSucceeds() {
        assertCreate(request("H", "HR", "Active"), "H", "HR", "Active");
    }

    // Create Department - UTCID29
    // Input code="H" (1 ký tự). Không tự áp quy tắc tối thiểu 2 ký tự của Role.
    @Test
    @DisplayName("Create Department - UTCID29")
    void create_withOneCharacterCodeSucceeds() {
        assertCreate(request("Human Resources", "H", "Active"), "Human Resources", "H", "Active");
    }

    // Create Department - UTCID30
    // Input name gồm 255 chữ N. Lưu/trả đủ 255 ký tự; không kiểm tra giới hạn database thật.
    @Test
    @DisplayName("Create Department - UTCID30")
    void create_withLongNamePreservesEveryCharacter() {
        assertCreate(request("N".repeat(255), "HR", "Active"), "N".repeat(255), "HR", "Active");
    }

    // Create Department - UTCID31
    // Input code gồm 255 chữ h. Kiểm tra trùng/lưu/trả đủ 255 chữ H.
    @Test
    @DisplayName("Create Department - UTCID31")
    void create_withLongCodeNormalizesWithoutTruncation() {
        assertCreate(request("Human Resources", "h".repeat(255), "Active"), "Human Resources", "H".repeat(255), "Active");
    }

    // Create Department - UTCID32
    // Input name: 255 chữ N và 2 khoảng trắng hai đầu. Kết quả có đúng 255 chữ N.
    @Test
    @DisplayName("Create Department - UTCID32")
    void create_withPaddedLongNameTrimsWithoutTruncation() {
        assertCreate(request(" " + "N".repeat(255) + " ", "HR", "Active"), "N".repeat(255), "HR", "Active");
    }

    // Create Department - UTCID33
    // Input code: 255 chữ h và 2 khoảng trắng hai đầu. Kết quả có đúng 255 chữ H.
    @Test
    @DisplayName("Create Department - UTCID33")
    void create_withPaddedLongCodeTrimsAndNormalizes() {
        assertCreate(request("Human Resources", " " + "h".repeat(255) + " ", "Active"), "Human Resources", "H".repeat(255), "Active");
    }

    // Update Department - UTCID01
    // ID ...0101 có HR. Đổi tên/mã/status; giữ ID, company, ngày tạo và đặt ngày sửa mới.
    @Test
    @DisplayName("Update Department - UTCID01")
    void update_withValidRequestUpdatesAndPreservesIdentity() {
        assertUpdate(request("People Operations", "PEOPLE", "Inactive"), "People Operations", "PEOPLE", "Inactive");
    }

    // Update Department - UTCID02
    // Mã hiện tại HR, input code="HR2" chưa bị dùng. Cho phép đổi mã phòng ban.
    @Test
    @DisplayName("Update Department - UTCID02")
    void update_allowsChangingCodeToUnusedCode() {
        assertUpdate(request("Human Resources", "HR2", "Active"), "Human Resources", "HR2", "Active");
    }

    // Update Department - UTCID03
    // Input giữ mã HR. Kiểm tra trùng phải loại chính ID ...0101, cập nhật thành công.
    @Test
    @DisplayName("Update Department - UTCID03")
    void update_withUnchangedCodeExcludesOwnId() {
        assertUpdate(request("Human Resources", "HR", "Active"), "Human Resources", "HR", "Active");
    }

    // Update Department - UTCID04
    // Input code="hr". Kiểm tra trùng và lưu bằng HR, không giữ mã chữ thường.
    @Test
    @DisplayName("Update Department - UTCID04")
    void update_normalizesLowercaseCode() {
        assertUpdate(request("Human Resources", "hr", "Active"), "Human Resources", "HR", "Active");
    }

    // Update Department - UTCID05
    // Input code="  HR  ". Trim mã trước kiểm tra trùng và lưu.
    @Test
    @DisplayName("Update Department - UTCID05")
    void update_trimsCode() {
        assertUpdate(request("Human Resources", "  HR  ", "Active"), "Human Resources", "HR", "Active");
    }

    // Update Department - UTCID06
    // Input code="  hR  ". Kết hợp trim và viết hoa thành HR.
    @Test
    @DisplayName("Update Department - UTCID06")
    void update_trimsAndNormalizesMixedCaseCode() {
        assertUpdate(request("Human Resources", "  hR  ", "Active"), "Human Resources", "HR", "Active");
    }

    // Update Department - UTCID07
    // Input name="  Human Resources  ". Tên lưu/trả về bỏ khoảng trắng hai đầu.
    @Test
    @DisplayName("Update Department - UTCID07")
    void update_trimsName() {
        assertUpdate(request("  Human Resources  ", "HR", "Active"), "Human Resources", "HR", "Active");
    }

    // Update Department - UTCID08
    // Input tên tiếng Việt có dấu và khoảng trắng hai đầu. Giữ đúng Phòng Hành chính.
    @Test
    @DisplayName("Update Department - UTCID08")
    void update_preservesVietnameseName() {
        assertUpdate(request("  Phòng Hành chính  ", "ADM", "Active"), "Phòng Hành chính", "ADM", "Active");
    }

    // Update Department - UTCID09
    // Phòng đang Active, input status="Inactive". Lưu/trả trạng thái Inactive.
    @Test
    @DisplayName("Update Department - UTCID09")
    void update_changesActiveToInactive() {
        assertUpdate(request("Human Resources", "HR", "Inactive"), "Human Resources", "HR", "Inactive");
    }

    // Update Department - UTCID10
    // Phòng đang Inactive, input status="Active". Kích hoạt lại phòng ban.
    @Test
    @DisplayName("Update Department - UTCID10")
    void update_changesInactiveToActive() {
        Departments department = existingDepartment();
        department.setDepartmentStatus("Inactive");
        assertUpdate(department, request("Human Resources", "HR", "Active"), "Human Resources", "HR", "Active");
    }

    // Update Department - UTCID11
    // Phòng đang Inactive, input status=null. Theo source hiện tại status mặc định thành Active.
    @Test
    @DisplayName("Update Department - UTCID11")
    void update_withNullStatusDefaultsToActive() {
        assertUpdateDefaultStatus(null);
    }

    // Update Department - UTCID12
    // Phòng đang Inactive, input status="". Theo source hiện tại đổi thành Active.
    @Test
    @DisplayName("Update Department - UTCID12")
    void update_withEmptyStatusDefaultsToActive() {
        assertUpdateDefaultStatus("");
    }

    // Update Department - UTCID13
    // Phòng đang Inactive, input status="   ". Dùng Active theo giá trị mặc định.
    @Test
    @DisplayName("Update Department - UTCID13")
    void update_withBlankStatusDefaultsToActive() {
        assertUpdateDefaultStatus("   ");
    }

    // Update Department - UTCID14
    // Input status chỉ gồm tab/xuống dòng. Dùng Active, không lưu chuỗi trắng.
    @Test
    @DisplayName("Update Department - UTCID14")
    void update_withWhitespaceControlStatusDefaultsToActive() {
        assertUpdateDefaultStatus("\t\n");
    }

    // Update Department - UTCID15
    // Input request=null. HTTP 400, lỗi thiếu request, không tìm hoặc lưu phòng ban.
    @Test
    @DisplayName("Update Department - UTCID15")
    void update_withNullRequestIsRejected() {
        assertUpdateValidation(null, "Department request is required");
    }

    // Update Department - UTCID16
    // Input name=null. HTTP 400 và lỗi tên bắt buộc; không truy cập repository.
    @Test
    @DisplayName("Update Department - UTCID16")
    void update_withNullNameIsRejected() {
        assertUpdateValidation(request(null, "HR", "Active"), "Department name is required");
    }

    // Update Department - UTCID17
    // Input name="". HTTP 400, không lưu tên rỗng.
    @Test
    @DisplayName("Update Department - UTCID17")
    void update_withEmptyNameIsRejected() {
        assertUpdateValidation(request("", "HR", "Active"), "Department name is required");
    }

    // Update Department - UTCID18
    // Input name="   ". HTTP 400 và lỗi tên bắt buộc.
    @Test
    @DisplayName("Update Department - UTCID18")
    void update_withBlankNameIsRejected() {
        assertUpdateValidation(request("   ", "HR", "Active"), "Department name is required");
    }

    // Update Department - UTCID19
    // Input name chỉ có tab/xuống dòng. HTTP 400, không gọi lưu.
    @Test
    @DisplayName("Update Department - UTCID19")
    void update_withWhitespaceControlNameIsRejected() {
        assertUpdateValidation(request("\t\n", "HR", "Active"), "Department name is required");
    }

    // Update Department - UTCID20
    // Input name là khoảng trắng Unicode U+2003. HTTP 400 và lỗi tên bắt buộc.
    @Test
    @DisplayName("Update Department - UTCID20")
    void update_withUnicodeBlankNameIsRejected() {
        assertUpdateValidation(request("\u2003", "HR", "Active"), "Department name is required");
    }

    // Update Department - UTCID21
    // Input code=null. HTTP 400 và lỗi mã bắt buộc.
    @Test
    @DisplayName("Update Department - UTCID21")
    void update_withNullCodeIsRejected() {
        assertUpdateValidation(request("Human Resources", null, "Active"), "Department code is required");
    }

    // Update Department - UTCID22
    // Input code="". HTTP 400, không tìm hoặc lưu phòng ban.
    @Test
    @DisplayName("Update Department - UTCID22")
    void update_withEmptyCodeIsRejected() {
        assertUpdateValidation(request("Human Resources", "", "Active"), "Department code is required");
    }

    // Update Department - UTCID23
    // Input code="   ". HTTP 400 và lỗi mã bắt buộc.
    @Test
    @DisplayName("Update Department - UTCID23")
    void update_withBlankCodeIsRejected() {
        assertUpdateValidation(request("Human Resources", "   ", "Active"), "Department code is required");
    }

    // Update Department - UTCID24
    // Input code chỉ có tab/xuống dòng. HTTP 400, không gọi lưu.
    @Test
    @DisplayName("Update Department - UTCID24")
    void update_withWhitespaceControlCodeIsRejected() {
        assertUpdateValidation(request("Human Resources", "\t\n", "Active"), "Department code is required");
    }

    // Update Department - UTCID25
    // Input code là khoảng trắng Unicode U+2003. HTTP 400 và lỗi mã bắt buộc.
    @Test
    @DisplayName("Update Department - UTCID25")
    void update_withUnicodeBlankCodeIsRejected() {
        assertUpdateValidation(request("Human Resources", "\u2003", "Active"), "Department code is required");
    }

    // Update Department - UTCID26
    // ID ...0101 không tồn tại. HTTP 400, không kiểm tra trùng và không gọi lưu.
    @Test
    @DisplayName("Update Department - UTCID26")
    void update_withMissingDepartmentIsRejected() {
        when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.empty());
        assertError(departmentController.updateDepartment(DEPARTMENT_ID, request("Human Resources", "HR", "Active")),
                HttpStatus.BAD_REQUEST, "Department not found with id: " + DEPARTMENT_ID);
        verify(departmentRepository).findById(DEPARTMENT_ID);
        verify(departmentRepository, never()).existsByDepartmentCodeIgnoreCaseAndIdNot(any(String.class), any(UUID.class));
        verify(departmentRepository, never()).save(any(Departments.class));
        verifyNoMoreInteractions(departmentRepository);
    }

    // Update Department - UTCID27
    // Phòng khác đang dùng IT, input code="IT". HTTP 400, giữ nguyên phòng HR, không lưu.
    @Test
    @DisplayName("Update Department - UTCID27")
    void update_withDuplicateUppercaseCodeIsRejected() {
        assertUpdateDuplicate("IT");
    }

    // Update Department - UTCID28
    // Phòng khác đang dùng IT, input code="it". Phát hiện trùng sau viết hoa, không sửa dữ liệu.
    @Test
    @DisplayName("Update Department - UTCID28")
    void update_withDuplicateLowercaseCodeIsRejected() {
        assertUpdateDuplicate("it");
    }

    // Update Department - UTCID29
    // Phòng khác đang dùng IT, input code="  iT  ". Phát hiện trùng sau trim/viết hoa.
    @Test
    @DisplayName("Update Department - UTCID29")
    void update_withDuplicatePaddedCodeIsRejected() {
        assertUpdateDuplicate("  iT  ");
    }

    // Update Department - UTCID30
    // Lỗi tìm ID ...0101. HTTP 400, giữ đúng lỗi Database unavailable, không lưu.
    @Test
    @DisplayName("Update Department - UTCID30")
    void update_whenFindFailsDoesNotSave() {
        when(departmentRepository.findById(DEPARTMENT_ID))
                .thenThrow(new RuntimeException("Database unavailable"));
        assertError(departmentController.updateDepartment(DEPARTMENT_ID, request("Human Resources", "HR", "Active")),
                HttpStatus.BAD_REQUEST, "Database unavailable");
        verify(departmentRepository).findById(DEPARTMENT_ID);
        verify(departmentRepository, never()).save(any(Departments.class));
        verifyNoMoreInteractions(departmentRepository);
    }

    // Update Department - UTCID31
    // Lỗi kiểm tra trùng mã. HTTP 400, phòng HR không bị đổi trường nào, không lưu.
    @Test
    @DisplayName("Update Department - UTCID31")
    void update_whenDuplicateCheckFailsPreservesDepartment() {
        Departments department = existingDepartment();
        Departments original = existingDepartment();
        when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByDepartmentCodeIgnoreCaseAndIdNot("IT", DEPARTMENT_ID))
                .thenThrow(new RuntimeException("Unable to check department code"));
        assertError(departmentController.updateDepartment(DEPARTMENT_ID, request("New name", "IT", "Inactive")),
                HttpStatus.BAD_REQUEST, "Unable to check department code");
        assertThat(department).usingRecursiveComparison().isEqualTo(original);
        verify(departmentRepository).findById(DEPARTMENT_ID);
        verify(departmentRepository).existsByDepartmentCodeIgnoreCaseAndIdNot("IT", DEPARTMENT_ID);
        verify(departmentRepository, never()).save(any(Departments.class));
        verifyNoMoreInteractions(departmentRepository);
    }

    // Update Department - UTCID32
    // Tìm/kiểm tra trùng thành công nhưng lưu lỗi. HTTP 400, không trả thành công giả.
    @Test
    @DisplayName("Update Department - UTCID32")
    void update_whenSaveFailsReturnsBadRequest() {
        Departments department = existingDepartment();
        when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByDepartmentCodeIgnoreCaseAndIdNot("HR", DEPARTMENT_ID)).thenReturn(false);
        when(departmentRepository.save(same(department))).thenThrow(new RuntimeException("Database unavailable"));
        assertError(departmentController.updateDepartment(DEPARTMENT_ID, request("Human Resources", "HR", "Active")),
                HttpStatus.BAD_REQUEST, "Database unavailable");
        InOrder calls = inOrder(departmentRepository);
        calls.verify(departmentRepository).findById(DEPARTMENT_ID);
        calls.verify(departmentRepository).existsByDepartmentCodeIgnoreCaseAndIdNot("HR", DEPARTMENT_ID);
        calls.verify(departmentRepository).save(same(department));
        verifyNoMoreInteractions(departmentRepository);
    }

    // Update Department - UTCID33
    // Phòng hiện chưa có company. Cập nhật thành công và giữ companyId=null.
    @Test
    @DisplayName("Update Department - UTCID33")
    void update_withNoCompanyKeepsNullCompany() {
        Departments department = existingDepartment();
        department.setCompany(null);
        assertUpdate(department, request("Human Resources", "HR", "Active"), "Human Resources", "HR", "Active");
    }

    // Update Department - UTCID34
    // Input name="H" (1 ký tự). Chấp nhận tên không rỗng và giữ đúng H.
    @Test
    @DisplayName("Update Department - UTCID34")
    void update_withOneCharacterNameSucceeds() {
        assertUpdate(request("H", "HR", "Active"), "H", "HR", "Active");
    }

    // Update Department - UTCID35
    // Input code="H" (1 ký tự). Cho phép mã ngắn theo source Department hiện tại.
    @Test
    @DisplayName("Update Department - UTCID35")
    void update_withOneCharacterCodeSucceeds() {
        assertUpdate(request("Human Resources", "H", "Active"), "Human Resources", "H", "Active");
    }

    // Update Department - UTCID36
    // Input name gồm 255 chữ N. Lưu/trả đúng và không cắt mất ký tự.
    @Test
    @DisplayName("Update Department - UTCID36")
    void update_withLongNamePreservesEveryCharacter() {
        assertUpdate(request("N".repeat(255), "HR", "Active"), "N".repeat(255), "HR", "Active");
    }

    // Update Department - UTCID37
    // Input code gồm 255 chữ h. Kiểm tra trùng/lưu/trả đủ 255 chữ H.
    @Test
    @DisplayName("Update Department - UTCID37")
    void update_withLongCodeNormalizesWithoutTruncation() {
        assertUpdate(request("Human Resources", "h".repeat(255), "Active"), "Human Resources", "H".repeat(255), "Active");
    }

    // Update Department - UTCID38
    // Input name có 255 chữ N và khoảng trắng hai đầu. Trim thành đúng 255 chữ N.
    @Test
    @DisplayName("Update Department - UTCID38")
    void update_withPaddedLongNameTrimsWithoutTruncation() {
        assertUpdate(request(" " + "N".repeat(255) + " ", "HR", "Active"), "N".repeat(255), "HR", "Active");
    }

    // Update Department - UTCID39
    // Input code có 255 chữ h và khoảng trắng hai đầu. Trim/viết hoa thành đúng 255 chữ H.
    @Test
    @DisplayName("Update Department - UTCID39")
    void update_withPaddedLongCodeTrimsAndNormalizes() {
        assertUpdate(request("Human Resources", " " + "h".repeat(255) + " ", "Active"), "Human Resources", "H".repeat(255), "Active");
    }

    private void assertSearch(String search, String status, String expectedSearch, String expectedStatus) {
        Departments department = existingDepartment();
        department.setDepartmentStatus("inactive".equals(expectedStatus) ? "Inactive" : "Active");
        assertSearchResult(search, status, expectedSearch, expectedStatus, List.of(department));
    }

    private void assertSearchResult(String search, String status, String expectedSearch, String expectedStatus,
                                    List<Departments> prepared) {
        when(departmentRepository.searchAndFilter(expectedSearch, expectedStatus)).thenReturn(prepared);
        List<DepartmentResponseDTO> result = assertResponse(
                departmentController.searchDepartments(search, status), HttpStatus.OK);
        assertThat(result).isNotNull().hasSize(prepared.size());
        for (int index = 0; index < prepared.size(); index++) {
            assertDto(result.get(index), prepared.get(index));
        }
        verify(departmentRepository).searchAndFilter(expectedSearch, expectedStatus);
        verifyNoMoreInteractions(departmentRepository);
    }

    private void assertGetAll(List<Departments> prepared) {
        when(departmentRepository.searchAndFilter("", "")).thenReturn(prepared);
        List<DepartmentResponseDTO> result = assertResponse(departmentController.getAllDepartments(), HttpStatus.OK);
        assertThat(result).isNotNull().hasSize(prepared.size());
        for (int index = 0; index < prepared.size(); index++) {
            assertDto(result.get(index), prepared.get(index));
        }
        verify(departmentRepository).searchAndFilter("", "");
        verifyNoMoreInteractions(departmentRepository);
    }

    private void assertView(Departments department) {
        Departments original = copyOf(department);
        when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(department));
        DepartmentResponseDTO result = assertResponse(departmentController.getDepartmentById(DEPARTMENT_ID), HttpStatus.OK);
        assertDto(result, original);
        assertThat(department).usingRecursiveComparison().isEqualTo(original);
        verify(departmentRepository).findById(DEPARTMENT_ID);
        verifyNoMoreInteractions(departmentRepository);
    }

    private void assertCreate(DepartmentRequestDTO request, String expectedName, String expectedCode, String expectedStatus) {
        when(departmentRepository.existsByDepartmentCodeIgnoreCase(expectedCode)).thenReturn(false);
        // Trả một entity khác với entity truyền vào save để kiểm tra dùng kết quả save.
        Departments saved = department(DEPARTMENT_ID, expectedCode, expectedName, expectedStatus, false);
        saved.setUpdatedAt(null);
        when(departmentRepository.save(any(Departments.class))).thenReturn(saved);
        LocalDateTime before = LocalDateTime.now();
        DepartmentResponseDTO result = assertResponse(departmentController.createDepartment(request), HttpStatus.CREATED);
        LocalDateTime after = LocalDateTime.now();
        ArgumentCaptor<Departments> captor = ArgumentCaptor.forClass(Departments.class);
        InOrder calls = inOrder(departmentRepository);
        calls.verify(departmentRepository).existsByDepartmentCodeIgnoreCase(expectedCode);
        calls.verify(departmentRepository).save(captor.capture());
        Departments submitted = captor.getValue();
        assertThat(submitted.getDepartmentName()).isEqualTo(expectedName);
        assertThat(submitted.getDepartmentCode()).isEqualTo(expectedCode);
        assertThat(submitted.getDepartmentStatus()).isEqualTo(expectedStatus);
        assertThat(submitted.getDepartmentCreatedAt()).isBetween(before, after);
        assertThat(submitted.getUpdatedAt()).isNull();
        assertThat(submitted.getCompany()).isNull();
        assertDto(result, saved);
        verifyNoMoreInteractions(departmentRepository);
    }

    private void assertCreateValidation(DepartmentRequestDTO request, String message) {
        assertError(departmentController.createDepartment(request), HttpStatus.BAD_REQUEST, message);
        verifyNoInteractions(departmentRepository);
    }

    private void assertCreateDuplicate(String code) {
        when(departmentRepository.existsByDepartmentCodeIgnoreCase("HR")).thenReturn(true);
        assertError(departmentController.createDepartment(request("Human Resources", code, "Active")),
                HttpStatus.BAD_REQUEST, "Department code is already in use!");
        verify(departmentRepository).existsByDepartmentCodeIgnoreCase("HR");
        verify(departmentRepository, never()).save(any(Departments.class));
        verifyNoMoreInteractions(departmentRepository);
    }

    private void assertUpdate(DepartmentRequestDTO request, String expectedName, String expectedCode, String expectedStatus) {
        assertUpdate(existingDepartment(), request, expectedName, expectedCode, expectedStatus);
    }

    private void assertUpdate(Departments department, DepartmentRequestDTO request,
                              String expectedName, String expectedCode, String expectedStatus) {
        Company originalCompany = department.getCompany();
        LocalDateTime originalCreatedAt = department.getDepartmentCreatedAt();
        UUID originalId = department.getId();
        when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByDepartmentCodeIgnoreCaseAndIdNot(expectedCode, DEPARTMENT_ID)).thenReturn(false);
        // Tách bản trả về của save để phát hiện việc bỏ qua kết quả repository.save().
        Departments saved = copyOf(department);
        saved.setDepartmentName(expectedName);
        saved.setDepartmentCode(expectedCode);
        saved.setDepartmentStatus(expectedStatus);
        saved.setUpdatedAt(UPDATED_AT.plusDays(1));
        when(departmentRepository.save(same(department))).thenReturn(saved);
        LocalDateTime before = LocalDateTime.now();
        DepartmentResponseDTO result = assertResponse(departmentController.updateDepartment(DEPARTMENT_ID, request), HttpStatus.OK);
        LocalDateTime after = LocalDateTime.now();
        assertThat(department.getId()).isEqualTo(originalId);
        assertThat(department.getCompany()).isSameAs(originalCompany);
        assertThat(department.getDepartmentCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(department.getDepartmentName()).isEqualTo(expectedName);
        assertThat(department.getDepartmentCode()).isEqualTo(expectedCode);
        assertThat(department.getDepartmentStatus()).isEqualTo(expectedStatus);
        assertThat(department.getUpdatedAt()).isBetween(before, after);
        assertDto(result, saved);
        InOrder calls = inOrder(departmentRepository);
        calls.verify(departmentRepository).findById(DEPARTMENT_ID);
        calls.verify(departmentRepository).existsByDepartmentCodeIgnoreCaseAndIdNot(expectedCode, DEPARTMENT_ID);
        calls.verify(departmentRepository).save(same(department));
        verifyNoMoreInteractions(departmentRepository);
    }

    private void assertUpdateDefaultStatus(String inputStatus) {
        Departments department = existingDepartment();
        department.setDepartmentStatus("Inactive");
        assertUpdate(department, request("Human Resources", "HR", inputStatus), "Human Resources", "HR", "Active");
    }

    private void assertUpdateValidation(DepartmentRequestDTO request, String message) {
        assertError(departmentController.updateDepartment(DEPARTMENT_ID, request), HttpStatus.BAD_REQUEST, message);
        verifyNoInteractions(departmentRepository);
    }

    private void assertUpdateDuplicate(String code) {
        Departments department = existingDepartment();
        Departments original = copyOf(department);
        when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByDepartmentCodeIgnoreCaseAndIdNot("IT", DEPARTMENT_ID)).thenReturn(true);
        assertError(departmentController.updateDepartment(DEPARTMENT_ID, request("New name", code, "Inactive")),
                HttpStatus.BAD_REQUEST, "Department code is already in use by another department!");
        assertThat(department).usingRecursiveComparison().isEqualTo(original);
        verify(departmentRepository).findById(DEPARTMENT_ID);
        verify(departmentRepository).existsByDepartmentCodeIgnoreCaseAndIdNot("IT", DEPARTMENT_ID);
        verify(departmentRepository, never()).save(any(Departments.class));
        verifyNoMoreInteractions(departmentRepository);
    }

    private static <T> T assertResponse(ResponseEntity<BaseResponse<T>> response, HttpStatus status) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(status.value());
        return response.getBody().getData();
    }

    private static void assertError(ResponseEntity<BaseResponse<DepartmentResponseDTO>> response,
                                    HttpStatus status, String message) {
        assertThat(assertResponse(response, status)).isNull();
        assertThat(response.getBody().getMessage()).isEqualTo(message);
    }

    private static void assertDto(DepartmentResponseDTO dto, Departments expected) {
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(expected.getId());
        assertThat(dto.getCompanyId()).isEqualTo(expected.getCompany() == null ? null : expected.getCompany().getId());
        assertThat(dto.getDepartmentName()).isEqualTo(expected.getDepartmentName());
        assertThat(dto.getDepartmentCode()).isEqualTo(expected.getDepartmentCode());
        assertThat(dto.getDepartmentStatus()).isEqualTo(expected.getDepartmentStatus());
        assertThat(dto.getDepartmentCreatedAt()).isEqualTo(expected.getDepartmentCreatedAt());
        assertThat(dto.getUpdatedAt()).isEqualTo(expected.getUpdatedAt());
    }

    private static DepartmentRequestDTO request(String name, String code, String status) {
        return new DepartmentRequestDTO(name, code, status);
    }

    private static Departments existingDepartment() {
        return department(DEPARTMENT_ID, "HR", "Human Resources", "Active", true);
    }

    private static Departments department(UUID id, String code, String name, String status, boolean withCompany) {
        Company company = null;
        if (withCompany) {
            company = new Company();
            company.setId(COMPANY_ID);
            company.setCompanyName("Test Company");
        }
        Departments department = Departments.builder()
                .departmentName(name).departmentCode(code).departmentStatus(status)
                .departmentCreatedAt(CREATED_AT).updatedAt(UPDATED_AT).company(company).build();
        department.setId(id);
        return department;
    }

    private static Departments copyOf(Departments source) {
        Departments result = Departments.builder()
                .departmentName(source.getDepartmentName()).departmentCode(source.getDepartmentCode())
                .departmentStatus(source.getDepartmentStatus()).departmentCreatedAt(source.getDepartmentCreatedAt())
                .updatedAt(source.getUpdatedAt()).company(source.getCompany())
                .users(source.getUsers()).proposals(source.getProposals()).build();
        result.setId(source.getId());
        return result;
    }
}
