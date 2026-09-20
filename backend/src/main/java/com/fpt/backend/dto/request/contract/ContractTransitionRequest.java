package com.fpt.backend.dto.request.contract;

import java.util.UUID;

public record ContractTransitionRequest(
<<<<<<< HEAD
        String action,
        String actorName,
        String actorRole,
        String comment,
        String signatureValue,
        UUID electronicSignatureId,
        String keyCode
) {
}
