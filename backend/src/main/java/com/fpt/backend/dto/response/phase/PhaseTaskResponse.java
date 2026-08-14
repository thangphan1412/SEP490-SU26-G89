package com.fpt.backend.dto.response.phase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhaseTaskResponse {
    private UUID id;
    private String title;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID assignedToId;
    private String assignedToName;
    private String assignedToEmail;
}
