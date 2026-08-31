package com.fpt.backend.controller.taskController;

import com.fpt.backend.constant.ApiConstant;
import com.fpt.backend.dto.request.task.TaskCreateRequest;
import com.fpt.backend.dto.request.task.TaskUpdateRequest;
import com.fpt.backend.dto.response.task.TaskItemResponse;
import com.fpt.backend.dto.response.task.TaskManagementResponse;
import com.fpt.backend.service.interfaces.task.ITaskService;
import com.fpt.backend.util.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstant.Task.TASKS)
@RequiredArgsConstructor
public class TaskController {
    private final ITaskService taskService;

    // Lấy dữ liệu quản lý task của một phase theo quyền truy cập hiện tại.
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment', 'Employee')")
    @GetMapping(ApiConstant.Task.BY_PHASE_ID)
    public ResponseEntity<BaseResponse<TaskManagementResponse>>
    getTasksByPhaseId(@PathVariable UUID phaseId) {
        return ResponseEntity.ok(new BaseResponse<>(taskService.getTasksByPhaseId(phaseId)));
    }

    // Tạo task mới trong phase sau khi request vượt qua Bean Validation.
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment', 'Employee')")
    @PostMapping(ApiConstant.Task.BY_PHASE_ID)
    public ResponseEntity<BaseResponse<TaskItemResponse>> createTask(
            @PathVariable UUID phaseId,
            @Valid @RequestBody TaskCreateRequest request) {
        TaskItemResponse createdTask = taskService.createTask(
                phaseId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>(HttpStatus.CREATED.value(), "Created", createdTask));
    }

    // Cập nhật task hiện có sau khi kiểm tra dữ liệu request.
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment', 'Employee')")
    @PutMapping(ApiConstant.Task.BY_ID)
    public ResponseEntity<BaseResponse<TaskItemResponse>> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(new BaseResponse<>(taskService.updateTask(taskId, request)));
    }

    // Đánh dấu task hoàn thành khi trạng thái và quyền truy cập hợp lệ.
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment', 'Employee')")
    @PatchMapping(ApiConstant.Task.MARK_DONE)
    public ResponseEntity<BaseResponse<TaskItemResponse>> markTaskAsDone(
            @PathVariable UUID taskId) {
        return ResponseEntity.ok(new BaseResponse<>(taskService.markTaskAsDone(taskId)));
    }
}
