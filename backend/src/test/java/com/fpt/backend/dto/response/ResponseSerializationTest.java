package com.fpt.backend.dto.response;

import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseSerializationTest {
    @Test
    void projectAccessResponse_keepsExistingBooleanJsonFieldNames() throws Exception {
        ProjectAccessResponse response = ProjectAccessResponse.builder()
                .projectId(UUID.randomUUID())
                .currentUserId(UUID.randomUUID())
                .isProjectCreator(true)
                .isProjectMember(true)
                .isExecutiveViewer(false)
                .allowedActions(List.of("VIEW_PROJECT"))
                .fullScopeActions(List.of("VIEW_PROJECT"))
                .workScope("FULL")
                .build();

        String json = JsonMapper.builder().build().writeValueAsString(response);

        assertThat(json)
                .contains("\"isProjectCreator\":true")
                .contains("\"isProjectMember\":true")
                .contains("\"isExecutiveViewer\":false")
                .doesNotContain("\"projectCreator\"")
                .doesNotContain("\"projectMember\"")
                .doesNotContain("\"executiveViewer\"");
    }
}
