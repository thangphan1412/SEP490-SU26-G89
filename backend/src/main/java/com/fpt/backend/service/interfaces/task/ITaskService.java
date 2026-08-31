package com.fpt.backend.service.interfaces.task;

import com.fpt.backend.dto.request.task.TaskCreateRequest;
import com.fpt.backend.dto.request.task.TaskUpdateRequest;
import com.fpt.backend.dto.response.task.TaskItemResponse;
import com.fpt.backend.dto.response.task.TaskManagementResponse;

import java.util.UUID;

public interface ITaskService {
    // Lấy dữ liệu quản lý task theo phase.
    TaskManagementResponse getTasksByPhaseId(UUID phaseId);

    // Tạo task mới trong một phase.
    TaskItemResponse createTask(UUID phaseId, TaskCreateRequest request);

    // Cập nhật task hiện có.
    TaskItemResponse updateTask(UUID taskId, TaskUpdateRequest request);

    // Đánh dấu task hiện có là hoàn thành.
    TaskItemResponse markTaskAsDone(UUID taskId);
}
