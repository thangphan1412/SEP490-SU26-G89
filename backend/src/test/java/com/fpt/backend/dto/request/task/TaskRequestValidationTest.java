package com.fpt.backend.dto.request.task;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class TaskRequestValidationTest {
    private static final LocalDate START_DATE = LocalDate.of(2026, 9, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 9, 10);

    private final Validator validator = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    // Kiểm tra tiêu đề task dài 254 ký tự, nhỏ hơn giới hạn 255, được chấp nhận.
    @Test
    void taskTitle_belowTwoHundredFiftyFiveCharacters_isValid() {
        assertThat(validator.validate(createRequest("T".repeat(254), START_DATE, END_DATE)))
                .isEmpty();
    }

    // Kiểm tra tiêu đề task dài đúng 255 ký tự vẫn được chấp nhận.
    @Test
    void taskTitle_exactlyTwoHundredFiftyFiveCharacters_isValid() {
        assertThat(validator.validate(createRequest("T".repeat(255), START_DATE, END_DATE)))
                .isEmpty();
    }

    // Kiểm tra tiêu đề task dài 256 ký tự bị từ chối vì vượt giới hạn.
    @Test
    void taskTitle_aboveTwoHundredFiftyFiveCharacters_isInvalid() {
        assertViolation(
                createRequest("T".repeat(256), START_DATE, END_DATE),
                "title"
        );
    }

    // Kiểm tra tiêu đề task chỉ chứa khoảng trắng bị từ chối.
    @Test
    void taskTitle_blankValue_isInvalid() {
        assertViolation(createRequest(" ", START_DATE, END_DATE), "title");
    }

    // Kiểm tra request tạo task bắt buộc phải có cả ngày bắt đầu và ngày kết thúc.
    @Test
    void taskCreateDates_nullValues_areInvalid() {
        assertThat(violationPaths(createRequest("Task", null, null)))
                .contains("startDate", "endDate");
    }

    // Kiểm tra request cập nhật task bắt buộc phải có trạng thái.
    @Test
    void taskUpdateStatus_nullValue_isInvalid() {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Task",
                START_DATE,
                END_DATE,
                null,
                null
        );

        assertViolation(request, "status");
    }

    private TaskCreateRequest createRequest(
            String title,
            LocalDate startDate,
            LocalDate endDate) {
        return new TaskCreateRequest(title, startDate, endDate, null);
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
