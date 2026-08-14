package com.fpt.backend.dto.response.task;

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
public class TaskItemResponse {
    private UUID id;
    private String title;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID assignedToId;
    private String assignedToName;
    private String assignedToEmail;
    private List<TaskContractResponse> contracts;
}
