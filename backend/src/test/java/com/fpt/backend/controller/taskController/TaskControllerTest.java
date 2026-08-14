package com.fpt.backend.controller.taskController;

import com.fpt.backend.dto.request.task.TaskCreateRequest;
import com.fpt.backend.dto.request.task.TaskUpdateRequest;
import com.fpt.backend.dto.response.task.TaskItemResponse;
import com.fpt.backend.dto.response.task.TaskManagementResponse;
import com.fpt.backend.enums.TaskStatus;
import com.fpt.backend.service.interfaces.task.ITaskService;
import com.fpt.backend.util.BaseResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private ITaskService taskService;

    @InjectMocks
    private TaskController taskController;

    // Kiểm tra API quản lý task trả đúng dữ liệu của phase và không cho phép cache.
    @Test
    void getTasksByPhaseId_existingPhase_returnsManagementDataWithoutCaching() {
        UUID phaseId = UUID.randomUUID();
        TaskManagementResponse management = TaskManagementResponse.builder()
                .phaseId(phaseId)
                .build();
        when(taskService.getTasksByPhaseId(phaseId)).thenReturn(management);

        ResponseEntity<BaseResponse<TaskManagementResponse>> response =
                taskController.getTasksByPhaseId(phaseId);

        assertOkResponse(response, management);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
        verify(taskService).getTasksByPhaseId(phaseId);
    }

    // Kiểm tra API tạo task trả HTTP 201 cùng task vừa được tạo.
    @Test
    void createTask_validRequest_returnsCreatedResponse() {
        UUID phaseId = UUID.randomUUID();
        TaskCreateRequest request = new TaskCreateRequest(
                "Prepare samples",
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 20),
                UUID.randomUUID()
        );
        TaskItemResponse created = TaskItemResponse.builder()
                .id(UUID.randomUUID())
                .title(request.title())
                .build();
        when(taskService.createTask(phaseId, request)).thenReturn(created);

        ResponseEntity<BaseResponse<TaskItemResponse>> response =
                taskController.createTask(phaseId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body.getStatus()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(body.getMessage()).isEqualTo("Created");
            assertThat(body.getData()).isSameAs(created);
        });
        verify(taskService).createTask(phaseId, request);
    }

    // Kiểm tra API cập nhật task trả HTTP 200 cùng dữ liệu task sau cập nhật.
    @Test
    void updateTask_validRequest_returnsUpdatedTask() {
        UUID taskId = UUID.randomUUID();
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Prepare final samples",
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 25),
                TaskStatus.IN_PROGRESS,
                UUID.randomUUID()
        );
        TaskItemResponse updated = TaskItemResponse.builder().id(taskId).build();
        when(taskService.updateTask(taskId, request)).thenReturn(updated);

        ResponseEntity<BaseResponse<TaskItemResponse>> response =
                taskController.updateTask(taskId, request);

        assertOkResponse(response, updated);
        verify(taskService).updateTask(taskId, request);
    }

    // Kiểm tra API đánh dấu hoàn thành trả về task có trạng thái DONE.
    @Test
    void markTaskAsDone_editableTask_returnsCompletedTask() {
        UUID taskId = UUID.randomUUID();
        TaskItemResponse completed = TaskItemResponse.builder()
                .id(taskId)
                .status(TaskStatus.DONE.name())
                .build();
        when(taskService.markTaskAsDone(taskId)).thenReturn(completed);

        ResponseEntity<BaseResponse<TaskItemResponse>> response =
                taskController.markTaskAsDone(taskId);

        assertOkResponse(response, completed);
        verify(taskService).markTaskAsDone(taskId);
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
