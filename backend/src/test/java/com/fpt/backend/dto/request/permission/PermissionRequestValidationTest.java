package com.fpt.backend.dto.request.permission;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionRequestValidationTest {
    private final Validator validator = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    // Kiểm tra tên permission dài 49 ký tự, nhỏ hơn giới hạn 50, được chấp nhận.
    @Test
    void permissionName_belowFiftyCharacters_isValid() {
        assertValid(request("N".repeat(49), "PERMISSION_CODE", "Description",
                List.of("VIEW_PROJECT"), "FULL"));
    }

    // Kiểm tra tên permission dài đúng 50 ký tự vẫn được chấp nhận.
    @Test
    void permissionName_exactlyFiftyCharacters_isValid() {
        assertValid(request("N".repeat(50), "PERMISSION_CODE", "Description",
                List.of("VIEW_PROJECT"), "FULL"));
    }

    // Kiểm tra tên permission dài 51 ký tự bị từ chối vì vượt giới hạn.
    @Test
    void permissionName_aboveFiftyCharacters_isInvalid() {
        assertViolation(
                request("N".repeat(51), "PERMISSION_CODE", "Description",
                        List.of("VIEW_PROJECT"), "FULL"),
                "permissionName"
        );
    }

    // Kiểm tra permission code dài 49 ký tự, nhỏ hơn giới hạn 50, được chấp nhận.
    @Test
    void permissionCode_belowFiftyCharacters_isValid() {
        assertValid(request("Task Manager", "C".repeat(49), "Description",
                List.of("VIEW_PROJECT"), "FULL"));
    }

    // Kiểm tra permission code dài đúng 50 ký tự vẫn được chấp nhận.
    @Test
    void permissionCode_exactlyFiftyCharacters_isValid() {
        assertValid(request("Task Manager", "C".repeat(50), "Description",
                List.of("VIEW_PROJECT"), "FULL"));
    }

    // Kiểm tra permission code dài 51 ký tự bị từ chối vì vượt giới hạn.
    @Test
    void permissionCode_aboveFiftyCharacters_isInvalid() {
        assertViolation(
                request("Task Manager", "C".repeat(51), "Description",
                        List.of("VIEW_PROJECT"), "FULL"),
                "permissionCode"
        );
    }

    // Kiểm tra mô tả permission dài 254 ký tự, nhỏ hơn giới hạn 255, được chấp nhận.
    @Test
    void permissionDescription_belowTwoHundredFiftyFiveCharacters_isValid() {
        assertValid(request("Task Manager", "TASK_MANAGER", "D".repeat(254),
                List.of("VIEW_PROJECT"), "FULL"));
    }

    // Kiểm tra mô tả permission dài đúng 255 ký tự vẫn được chấp nhận.
    @Test
    void permissionDescription_exactlyTwoHundredFiftyFiveCharacters_isValid() {
        assertValid(request("Task Manager", "TASK_MANAGER", "D".repeat(255),
                List.of("VIEW_PROJECT"), "FULL"));
    }

    // Kiểm tra mô tả permission dài 256 ký tự bị từ chối vì vượt giới hạn.
    @Test
    void permissionDescription_aboveTwoHundredFiftyFiveCharacters_isInvalid() {
        assertViolation(
                request("Task Manager", "TASK_MANAGER", "D".repeat(256),
                        List.of("VIEW_PROJECT"), "FULL"),
                "permissionDescription"
        );
    }

    // Kiểm tra từng action code trong danh sách permission không được để trống.
    @Test
    void allowedActions_blankElement_isInvalid() {
        assertViolation(
                request("Task Manager", "TASK_MANAGER", "Description",
                        List.of(" "), "FULL"),
                "allowedActions[0].<list element>"
        );
    }

    // Kiểm tra work scope chỉ chấp nhận OWN hoặc FULL.
    @Test
    void workScope_unsupportedValue_isInvalid() {
        assertViolation(
                request("Task Manager", "TASK_MANAGER", "Description",
                        List.of("VIEW_PROJECT"), "PROJECT"),
                "workScope"
        );
    }

    private PermissionRequest request(
            String name,
            String code,
            String description,
            List<String> allowedActions,
            String workScope) {
        return new PermissionRequest(
                name,
                code,
                description,
                true,
                UUID.randomUUID(),
                allowedActions,
                workScope
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
