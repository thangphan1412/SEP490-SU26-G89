package com.fpt.backend.service.impl.contract;

import java.time.LocalDateTime;

public record ContractSigningEmailEvent(
        Type type,
        String contractNumber,
        String contractTitle,
        String signerName,
        String signerEmail,
        String recipientName,
        String recipientEmail,
        String publicKey,
        LocalDateTime signedAt
) {
    public enum Type {
        CEO_SIGNED,
        PARTNER_SIGNED
    }
}
