package com.fpt.backend.dto.response.task;

import com.fpt.backend.enums.TaskStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TaskManagementResponse(
        UUID phaseId,
        String phaseTitle,
        LocalDate phaseStartDate,
        LocalDate phaseEndDate,
        UUID projectId,
        String projectName,
        boolean fullWorkScope,
        boolean canCreateTasks,
        boolean canApproveTasks,
        List<TaskStatus> statusOptions,
        List<TaskMemberOptionResponse> memberOptions,
        List<TaskItemResponse> tasks
) {
}
