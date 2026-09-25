package com.fpt.backend.enums;

import java.util.Locale;

public enum ContractAction {
    COMPLETE_STEP,
    CANCEL,
    REJECT,
    // Thanh lý hợp đồng đã đủ chữ ký (ACTIVE / OVERDUE / PENDING_EFFECTIVE)
    SETTLE,
    // Gia hạn thời hạn ký khi còn bên chưa ký
    EXTEND_SIGNING_DEADLINE;

    public static ContractAction fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Contract action is required");
        }

        return ContractAction.valueOf(
                value.trim().toUpperCase(Locale.ROOT).replace(' ', '_')
        );
    }
}
