package com.fpt.backend.dto.request.project;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectCreateRequestValidationTest {
    private static final String VALID_PROJECT_NAME = "Winter Collection";
    private static final String VALID_PROJECT_CODE = "PRJ-2026-Winter Collection";
    private static final LocalDate START_DATE = LocalDate.of(2026, 9, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 10, 31);

    private final Validator validator = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    // Kiểm tra tên dự án dài 49 ký tự, nhỏ hơn giới hạn 50, được chấp nhận.
    @Test
    void projectName_belowFiftyCharacters_isValid() {
        assertValid(requestWithProjectName("A".repeat(49)));
    }

    // Kiểm tra tên dự án dài đúng 50 ký tự vẫn được chấp nhận.
    @Test
    void projectName_exactlyFiftyCharacters_isValid() {
        assertValid(requestWithProjectName("A".repeat(50)));
    }

    // Kiểm tra tên dự án dài 51 ký tự bị từ chối vì vượt giới hạn.
    @Test
    void projectName_aboveFiftyCharacters_isInvalid() {
        assertViolation(requestWithProjectName("A".repeat(51)), "projectName");
    }

    // Kiểm tra tên dự án chỉ chứa khoảng trắng bị từ chối.
    @Test
    void projectName_blankValue_isInvalid() {
        assertViolation(requestWithProjectName("   "), "projectName");
    }

    // Kiểm tra project code dài 49 ký tự, đúng pattern và dưới giới hạn được chấp nhận.
    @Test
    void projectCode_belowFiftyCharacters_isValid() {
        assertValid(requestWithProjectCode(projectCodeOfLength(49)));
    }

    // Kiểm tra project code dài đúng 50 ký tự và đúng pattern được chấp nhận.
    @Test
    void projectCode_exactlyFiftyCharacters_isValid() {
        assertValid(requestWithProjectCode(projectCodeOfLength(50)));
    }

    // Kiểm tra project code dài 51 ký tự bị từ chối vì vượt giới hạn.
    @Test
    void projectCode_aboveFiftyCharacters_isInvalid() {
        assertViolation(requestWithProjectCode(projectCodeOfLength(51)), "projectCode");
    }

    // Kiểm tra project code không bắt đầu bằng PRJ-yyyy- bị từ chối.
    @Test
    void projectCode_wrongFormat_isInvalid() {
        assertViolation(requestWithProjectCode("PROJECT-2026-Winter Collection"), "projectCode");
    }

    // Kiểm tra project code rỗng bị từ chối bởi ràng buộc bắt buộc nhập.
    @Test
    void projectCode_blankValue_isInvalid() {
        assertViolation(requestWithProjectCode(" "), "projectCode");
    }

    // Kiểm tra ngày bắt đầu dự án là bắt buộc khi tạo mới.
    @Test
    void projectStartDate_nullValue_isInvalid() {
        assertViolation(requestWithStartDate(null), "projectStartDate");
    }

    // Kiểm tra ngày kết thúc dự án là bắt buộc khi tạo mới.
    @Test
    void projectEndDate_nullValue_isInvalid() {
        assertViolation(requestWithEndDate(null), "projectEndDate");
    }

    // Kiểm tra mô tả dự án dài 254 ký tự, nhỏ hơn giới hạn 255, được chấp nhận.
    @Test
    void projectDescription_belowTwoHundredFiftyFiveCharacters_isValid() {
        assertValid(requestWithDescription("D".repeat(254)));
    }

    // Kiểm tra mô tả dự án dài đúng 255 ký tự vẫn được chấp nhận.
    @Test
    void projectDescription_exactlyTwoHundredFiftyFiveCharacters_isValid() {
        assertValid(requestWithDescription("D".repeat(255)));
    }

    // Kiểm tra mô tả dự án dài 256 ký tự bị từ chối vì vượt giới hạn.
    @Test
    void projectDescription_aboveTwoHundredFiftyFiveCharacters_isInvalid() {
        assertViolation(requestWithDescription("D".repeat(256)), "projectDescription");
    }

    // Kiểm tra mô tả dự án được phép để null vì đây không phải trường bắt buộc.
    @Test
    void projectDescription_nullValue_isValid() {
        assertValid(requestWithDescription(null));
    }

    // Kiểm tra danh sách phase là bắt buộc khi tạo dự án.
    @Test
    void phases_nullValue_isInvalid() {
        assertViolation(requestWithPhases(null), "phases");
    }

    // Kiểm tra danh sách phase rỗng bị từ chối.
    @Test
    void phases_emptyList_isInvalid() {
        assertViolation(requestWithPhases(List.of()), "phases");
    }

    // Kiểm tra danh sách phase không được chứa phần tử null.
    @Test
    void phases_nullElement_isInvalid() {
        assertViolation(
                requestWithPhases(Collections.singletonList(null)),
                "phases[0].<list element>"
        );
    }

    // Kiểm tra tiêu đề phase dài 149 ký tự, nhỏ hơn giới hạn 150, được chấp nhận.
    @Test
    void phaseTitle_belowOneHundredFiftyCharacters_isValid() {
        assertValid(requestWithPhase(phase("T".repeat(149), "Description", END_DATE)));
    }

    // Kiểm tra tiêu đề phase dài đúng 150 ký tự vẫn được chấp nhận.
    @Test
    void phaseTitle_exactlyOneHundredFiftyCharacters_isValid() {
        assertValid(requestWithPhase(phase("T".repeat(150), "Description", END_DATE)));
    }

    // Kiểm tra tiêu đề phase dài 151 ký tự bị từ chối vì vượt giới hạn.
    @Test
    void phaseTitle_aboveOneHundredFiftyCharacters_isInvalid() {
        assertViolation(
                requestWithPhase(phase("T".repeat(151), "Description", END_DATE)),
                "phases[0].title"
        );
    }

    // Kiểm tra tiêu đề phase chỉ chứa khoảng trắng bị từ chối.
    @Test
    void phaseTitle_blankValue_isInvalid() {
        assertViolation(
                requestWithPhase(phase(" ", "Description", END_DATE)),
                "phases[0].title"
        );
    }

    // Kiểm tra mô tả phase dài 499 ký tự, nhỏ hơn giới hạn 500, được chấp nhận.
    @Test
    void phaseDescription_belowFiveHundredCharacters_isValid() {
        assertValid(requestWithPhase(phase("Planning", "D".repeat(499), END_DATE)));
    }

    // Kiểm tra mô tả phase dài đúng 500 ký tự vẫn được chấp nhận.
    @Test
    void phaseDescription_exactlyFiveHundredCharacters_isValid() {
        assertValid(requestWithPhase(phase("Planning", "D".repeat(500), END_DATE)));
    }

    // Kiểm tra mô tả phase dài 501 ký tự bị từ chối vì vượt giới hạn.
    @Test
    void phaseDescription_aboveFiveHundredCharacters_isInvalid() {
        assertViolation(
                requestWithPhase(phase("Planning", "D".repeat(501), END_DATE)),
                "phases[0].description"
        );
    }

    // Kiểm tra mô tả phase được phép để null.
    @Test
    void phaseDescription_nullValue_isValid() {
        assertValid(requestWithPhase(phase("Planning", null, END_DATE)));
    }

    // Kiểm tra ngày kết thúc phase là trường bắt buộc.
    @Test
    void phaseEndDate_nullValue_isInvalid() {
        assertViolation(
                requestWithPhase(phase("Planning", "Description", null)),
                "phases[0].endDate"
        );
    }

    // Kiểm tra danh sách thành viên được phép để null khi tạo dự án.
    @Test
    void members_nullValue_isValid() {
        assertValid(requestWithMembers(null));
    }

    // Kiểm tra danh sách thành viên không được chứa phần tử null.
    @Test
    void members_nullElement_isInvalid() {
        assertViolation(
                requestWithMembers(Collections.singletonList(null)),
                "members[0].<list element>"
        );
    }

    // Kiểm tra mỗi thành viên dự án bắt buộc phải có user ID.
    @Test
    void memberUserId_nullValue_isInvalid() {
        assertViolation(
                requestWithMembers(List.of(new ProjectMemberRequest(null, null))),
                "members[0].userId"
        );
    }

    private ProjectCreateRequest requestWithProjectName(String projectName) {
        return request(projectName, VALID_PROJECT_CODE, START_DATE, END_DATE,
                "Description", validPhases(), validMembers());
    }

    private ProjectCreateRequest requestWithProjectCode(String projectCode) {
        return request(VALID_PROJECT_NAME, projectCode, START_DATE, END_DATE,
                "Description", validPhases(), validMembers());
    }

    private ProjectCreateRequest requestWithStartDate(LocalDate startDate) {
        return request(VALID_PROJECT_NAME, VALID_PROJECT_CODE, startDate, END_DATE,
                "Description", validPhases(), validMembers());
    }

    private ProjectCreateRequest requestWithEndDate(LocalDate endDate) {
        return request(VALID_PROJECT_NAME, VALID_PROJECT_CODE, START_DATE, endDate,
                "Description", validPhases(), validMembers());
    }

    private ProjectCreateRequest requestWithDescription(String description) {
        return request(VALID_PROJECT_NAME, VALID_PROJECT_CODE, START_DATE, END_DATE,
                description, validPhases(), validMembers());
    }

    private ProjectCreateRequest requestWithPhases(List<ProjectPhaseRequest> phases) {
        return request(VALID_PROJECT_NAME, VALID_PROJECT_CODE, START_DATE, END_DATE,
                "Description", phases, validMembers());
    }

    private ProjectCreateRequest requestWithPhase(ProjectPhaseRequest phase) {
        return requestWithPhases(List.of(phase));
    }

    private ProjectCreateRequest requestWithMembers(List<ProjectMemberRequest> members) {
        return request(VALID_PROJECT_NAME, VALID_PROJECT_CODE, START_DATE, END_DATE,
                "Description", validPhases(), members);
    }

    private ProjectCreateRequest request(
            String projectName,
            String projectCode,
            LocalDate startDate,
            LocalDate endDate,
            String description,
            List<ProjectPhaseRequest> phases,
            List<ProjectMemberRequest> members) {
        return new ProjectCreateRequest(
                projectName,
                projectCode,
                startDate,
                endDate,
                description,
                phases,
                members
        );
    }

    private List<ProjectPhaseRequest> validPhases() {
        return List.of(phase("Planning", "Description", END_DATE));
    }

    private List<ProjectMemberRequest> validMembers() {
        return List.of(new ProjectMemberRequest(UUID.randomUUID(), null));
    }

    private ProjectPhaseRequest phase(String title, String description, LocalDate endDate) {
        return new ProjectPhaseRequest(null, title, description, endDate);
    }

    private String projectCodeOfLength(int totalLength) {
        String prefix = "PRJ-2026-";
        return prefix + "A".repeat(totalLength - prefix.length());
    }

    private void assertValid(Object request) {
        assertThat(validator.validate(request)).isEmpty();
    }

    private void assertViolation(Object request, String expectedPath) {
        assertThat(violationPaths(request)).contains(expectedPath);
    }

    private Set<String> violationPaths(Object request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }
}
