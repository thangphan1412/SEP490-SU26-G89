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
public class PhaseDeliverableResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private String status;
}
