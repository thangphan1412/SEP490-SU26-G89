package com.fpt.backend.dto.request.contract;

import java.util.UUID;

public record ContractTransitionRequest(
                String action,
                String actorName,
                String actorRole,
                String comment,
                UUID electronicSignatureId) {
}
