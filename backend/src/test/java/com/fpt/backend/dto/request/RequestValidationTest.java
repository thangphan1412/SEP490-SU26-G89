package com.fpt.backend.dto.request;

import com.fpt.backend.dto.request.permission.PermissionListRequest;
import com.fpt.backend.dto.request.permission.PermissionRequest;
import com.fpt.backend.dto.request.project.ProjectCreateRequest;
import com.fpt.backend.dto.request.project.ProjectMemberRequest;
import com.fpt.backend.dto.request.project.ProjectPhaseRequest;
import com.fpt.backend.dto.request.project.ProjectUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidationTest {
    private final Validator validator = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    @Test
    void projectCreateRequest_validatesProjectAndNestedPhaseFields() {
        ProjectCreateRequest request = new ProjectCreateRequest(
                " ",
                "PRJ-2026-Project 01",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "Description",
                List.of(new ProjectPhaseRequest(null, " ", null, null)),
                List.of(new ProjectMemberRequest(null, null))
        );

        assertThat(violationPaths(request)).contains(
                "projectName",
                "phases[0].title",
                "phases[0].endDate",
                "members[0].userId"
        );
    }

    @Test
    void projectCode_acceptsExpectedVietnameseFormat() {
        ProjectUpdateRequest request = new ProjectUpdateRequest(
                null,
                "PRJ-2026-Thời trang mùa đông",
                null,
                null,
                null,
                null,
                null
        );

        assertThat(violationPaths(request)).doesNotContain("projectCode");
    }

    @Test
    void projectCode_rejectsInvalidFormats() {
        List<String> invalidCodes = List.of(
                "PROJECT-2026-Thời trang mùa đông",
                "PRJ-26-Thời trang mùa đông",
                "PRJ-2026-",
                "PRJ-2026----",
                "PRJ-2026- Thời trang mùa đông",
                "PRJ-2026-Thời trang mùa đông "
        );

        assertThat(invalidCodes).allSatisfy(projectCode -> {
            ProjectUpdateRequest request = new ProjectUpdateRequest(
                    null,
                    projectCode,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            assertThat(violationPaths(request)).contains("projectCode");
        });
    }

    @Test
    void projectUpdateRequest_allowsMembersOnlyUpdate() {
        ProjectUpdateRequest request = new ProjectUpdateRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(new ProjectMemberRequest(UUID.randomUUID(), null))
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void permissionRequest_validatesRequiredFieldsAndActionElements() {
        PermissionRequest request = new PermissionRequest(
                " ",
                " ",
                null,
                null,
                null,
                List.of(" "),
                "INVALID"
        );

        assertThat(violationPaths(request)).contains(
                "permissionName",
                "permissionCode",
                "status",
                "projectId",
                "allowedActions[0].<list element>",
                "workScope"
        );
    }

    @Test
    void permissionListRequest_rejectsInvalidPagingAndSorting() {
        PermissionListRequest request = new PermissionListRequest(
                null,
                null,
                null,
                -1,
                "unknownField",
                "sideways"
        );

        assertThat(violationPaths(request)).contains(
                "page",
                "sortBy",
                "sortDirection"
        );
    }

    private Set<String> violationPaths(Object request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }
}
