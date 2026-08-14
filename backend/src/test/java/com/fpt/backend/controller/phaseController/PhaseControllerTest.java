package com.fpt.backend.controller.phaseController;

import com.fpt.backend.dto.response.phase.PhaseDetailResponse;
import com.fpt.backend.dto.response.phase.PhaseListItemResponse;
import com.fpt.backend.service.interfaces.phase.IPhaseService;
import com.fpt.backend.util.BaseResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhaseControllerTest {

    @Mock
    private IPhaseService phaseService;

    @InjectMocks
    private PhaseController phaseController;

    // Kiểm tra API lấy phase theo dự án trả đúng danh sách và không cho phép cache.
    @Test
    void getPhasesByProject_existingProject_returnsPhasesWithoutCaching() {
        UUID projectId = UUID.randomUUID();
        List<PhaseListItemResponse> phases = List.of(
                PhaseListItemResponse.builder().id(UUID.randomUUID()).title("Planning").build()
        );
        when(phaseService.getPhasesByProjectId(projectId)).thenReturn(phases);

        ResponseEntity<BaseResponse<List<PhaseListItemResponse>>> response =
                phaseController.getPhasesByProject(projectId);

        assertOkResponse(response, phases);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
        verify(phaseService).getPhasesByProjectId(projectId);
    }

    // Kiểm tra API chi tiết phase tồn tại trả đúng dữ liệu và không cho phép cache.
    @Test
    void getPhaseById_existingPhase_returnsDetailWithoutCaching() {
        UUID phaseId = UUID.randomUUID();
        PhaseDetailResponse detail = PhaseDetailResponse.builder()
                .id(phaseId)
                .title("Planning")
                .build();
        when(phaseService.getPhaseById(phaseId)).thenReturn(detail);

        ResponseEntity<BaseResponse<PhaseDetailResponse>> response =
                phaseController.getPhaseById(phaseId);

        assertOkResponse(response, detail);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
        verify(phaseService).getPhaseById(phaseId);
    }

    private static <T> void assertOkResponse(
            ResponseEntity<BaseResponse<T>> response,
            T expectedData) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(body.getMessage()).isEqualTo(HttpStatus.OK.getReasonPhrase());
            assertThat(body.getData()).isSameAs(expectedData);
        });
    }
}
