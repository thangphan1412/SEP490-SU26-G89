package com.fpt.backend.dto.response.phase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhaseContractResponse {
    private UUID id;
    private String contractNumber;
    private String contractTitle;
    private String contractStatus;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private LocalDateTime linkedAt;
}
