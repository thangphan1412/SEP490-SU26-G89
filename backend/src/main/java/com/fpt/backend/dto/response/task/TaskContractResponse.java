package com.fpt.backend.dto.response.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskContractResponse {
    private UUID id;
    private String contractNumber;
    private String contractTitle;
}
