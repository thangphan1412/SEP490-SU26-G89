package com.fpt.backend.dto.request.project;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectUpdateRequestValidationTest {
    private final Validator validator = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    // Kiểm tra tên dự án được phép để null trong request cập nhật một phần.
    @Test
    void projectName_nullValue_isValidForPartialUpdate() {
        assertValid(request(null, "PRJ-2026-Update", null));
    }

    // Kiểm tra tên dự án chỉ chứa khoảng trắng bị từ chối khi được gửi lên để cập nhật.
    @Test
    void projectName_blankValue_isInvalid() {
        assertViolation(request("   ", null, null), "projectName");
    }

    // Kiểm tra tên dự án dài đúng 50 ký tự vẫn hợp lệ khi cập nhật.
    @Test
    void projectName_exactlyFiftyCharacters_isValid() {
        assertValid(request("A".repeat(50), null, null));
    }

    // Kiểm tra tên dự án dài 51 ký tự bị từ chối khi cập nhật.
    @Test
    void projectName_aboveFiftyCharacters_isInvalid() {
        assertViolation(request("A".repeat(51), null, null), "projectName");
    }

    // Kiểm tra project code được phép để null nếu request không cập nhật trường này.
    @Test
    void projectCode_nullValue_isValidForPartialUpdate() {
        assertValid(request("Updated Project", null, null));
    }

    // Kiểm tra khi đã gửi trường phases thì danh sách không được rỗng.
    @Test
    void phases_emptyList_isInvalid() {
        assertViolation(request(null, null, List.of()), "phases");
    }

    private ProjectUpdateRequest request(
            String projectName,
            String projectCode,
            List<ProjectPhaseRequest> phases) {
        return new ProjectUpdateRequest(
                projectName,
                projectCode,
                null,
                null,
                null,
                phases,
                null
        );
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
