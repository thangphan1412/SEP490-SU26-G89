package com.fpt.backend.enums;

import java.util.Locale;

public enum ContractAction {
    COMPLETE_STEP,
    CANCEL,
    REJECT;

    public static ContractAction fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Contract action is required");
        }

        return ContractAction.valueOf(
                value.trim().toUpperCase(Locale.ROOT).replace(' ', '_')
        );
    }
}
