package com.fpt.backend.dto.request.contract;

public record ContractTransitionRequest(
        String action,
        String actorName,
        String actorRole,
        String comment
) {
}
