package com.fpt.backend.enums;

import java.util.Locale;

public enum ContractWorkflowActionType {
    CREATE,
    APPROVE,
    SIGN,
    APPROVE_AND_SIGN;

    public boolean requiresSignature() {
        return this == SIGN || this == APPROVE_AND_SIGN;
    }

    public static ContractWorkflowActionType fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Workflow action type is required");
        }

        return ContractWorkflowActionType.valueOf(
                value.trim().toUpperCase(Locale.ROOT).replace(' ', '_')
        );
    }
}
