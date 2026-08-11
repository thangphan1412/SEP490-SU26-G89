package com.fpt.backend.service.interfaces.task;

import com.fpt.backend.dto.request.task.TaskCreateRequest;
import com.fpt.backend.dto.request.task.TaskUpdateRequest;
import com.fpt.backend.dto.response.task.TaskItemResponse;
import com.fpt.backend.dto.response.task.TaskManagementResponse;

import java.util.UUID;

public interface ITaskService {
    TaskManagementResponse getTasksByPhaseId(UUID phaseId);

    TaskItemResponse createTask(UUID phaseId, TaskCreateRequest request);

    TaskItemResponse updateTask(UUID taskId, TaskUpdateRequest request);

    TaskItemResponse markTaskAsDone(UUID taskId);
}
