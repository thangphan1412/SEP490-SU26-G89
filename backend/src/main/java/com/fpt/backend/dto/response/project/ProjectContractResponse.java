package com.fpt.backend.dto.response.project;

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
public class ProjectContractResponse {
    private UUID id;
    private String contractTitle;
    private String contractNumber;
    private String contractStatus;
}
