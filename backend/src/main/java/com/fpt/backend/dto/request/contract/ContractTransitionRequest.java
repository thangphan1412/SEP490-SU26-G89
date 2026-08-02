package com.fpt.backend.dto.request.contract;

import java.time.LocalDate;

public record ContractTransitionRequest(
        String action,
        String actorName,
        String actorRole,
        String comment,
        LocalDate signerDateOfBirth
) {
}
