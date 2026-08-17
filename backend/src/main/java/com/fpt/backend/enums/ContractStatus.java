package com.fpt.backend.enums;

import java.util.Locale;

public enum ContractStatus {
    NEW,
    PENDING_APPROVAL,
    PENDING_SIGNATURE,
    PENDING_INTERNAL_APPROVAL,
    PENDING_DIRECTOR_SIGNATURE,
    PENDING_PARTNER_SIGNATURE,
    SIGNED,
    ACTIVE,
    ENDED,
    CANCELLED;

    public boolean isTerminal() {
        return this == SIGNED || this == ENDED || this == CANCELLED;
    }

    public static ContractStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return NEW;
        }

        String normalized = value.trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_');

        return switch (normalized) {
            case "DRAFT" -> NEW;
            case "PENDING" -> PENDING_INTERNAL_APPROVAL;
            case "REJECTED" -> CANCELLED;
            case "COMPLETED", "EXPIRED", "END" -> ENDED;
            case "CANCELED", "CANCEL" -> CANCELLED;
            default -> ContractStatus.valueOf(normalized);
        };
    }
}
