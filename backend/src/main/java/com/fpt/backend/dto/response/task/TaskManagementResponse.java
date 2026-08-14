package com.fpt.backend.dto.response.task;

import com.fpt.backend.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskManagementResponse {
    private UUID phaseId;
    private String phaseTitle;
    private LocalDate phaseStartDate;
    private LocalDate phaseEndDate;
    private UUID projectId;
    private String projectName;
    private boolean fullWorkScope;
    private boolean canCreateTasks;
    private boolean canApproveTasks;
    private List<TaskStatus> statusOptions;
    private List<TaskMemberOptionResponse> memberOptions;
    private List<TaskItemResponse> tasks;
}
