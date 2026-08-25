package com.fpt.backend.dto.request.contract;

import java.util.UUID;

public record ContractTransitionRequest(
<<<<<<< HEAD
        String action,
        String actorName,
        String actorRole,
        String comment,
<<<<<<< HEAD
        UUID workflowStepId,
        UUID electronicSignatureId,
        String digitalSignature
=======
        UUID electronicSignatureId
>>>>>>> 7d6eb51fe9c660b46d1a1bc0200bcbbc73cf5f51
) {
=======
                String action,
                String actorName,
                String actorRole,
                String comment,
                UUID electronicSignatureId) {
>>>>>>> origin
}
