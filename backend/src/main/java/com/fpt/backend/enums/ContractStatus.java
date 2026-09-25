package com.fpt.backend.enums;

import java.util.Locale;

public enum ContractStatus {
    NEW,
    PENDING_APPROVAL,
    PENDING_SIGNATURE,
    PENDING_INTERNAL_APPROVAL,
    PENDING_DIRECTOR_SIGNATURE,
    PENDING_PARTNER_SIGNATURE,
    // Legacy: hợp đồng ký xong trước khi có PENDING_EFFECTIVE/ACTIVE, scheduler sẽ chuyển tiếp
    SIGNED,
    // Đã đủ chữ ký, chưa tới ngày hiệu lực
    PENDING_EFFECTIVE,
    // Đã đủ chữ ký và đang trong thời gian hiệu lực
    ACTIVE,
    // Quá expiration date nhưng chưa thanh lý
    OVERDUE,
    // Đã thanh lý (kết thúc hợp lệ)
    SETTLED,
    // Legacy: kết thúc tự động theo cơ chế cũ
    ENDED,
    // Quá hạn ký mà chưa đủ chữ ký các bên
    SIGNING_EXPIRED,
    CANCELLED;

    public boolean isTerminal() {
        return this == SETTLED
                || this == ENDED
                || this == SIGNING_EXPIRED
                || this == CANCELLED;
    }

    // Tất cả các bên đã ký: không được huỷ nữa, chỉ được thanh lý
    public boolean isFullySigned() {
        return this == SIGNED
                || this == PENDING_EFFECTIVE
                || this == ACTIVE
                || this == OVERDUE;
    }

    public boolean canSettle() {
        return isFullySigned();
    }

    public boolean isPendingSignature() {
        return this == PENDING_SIGNATURE
                || this == PENDING_DIRECTOR_SIGNATURE
                || this == PENDING_PARTNER_SIGNATURE;
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
            case "OVER_DUE" -> OVERDUE;
            case "LIQUIDATED", "SETTLEMENT" -> SETTLED;
            case "CANCELED", "CANCEL" -> CANCELLED;
            default -> ContractStatus.valueOf(normalized);
        };
    }
}
