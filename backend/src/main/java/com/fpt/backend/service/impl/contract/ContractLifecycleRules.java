package com.fpt.backend.service.impl.contract;

import com.fpt.backend.enums.ContractStatus;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Quy tắc thuần (không phụ thuộc DB) cho vòng đời hợp đồng sau khi ký.
 */
public final class ContractLifecycleRules {
    public static final ZoneId CONTRACT_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final int DEFAULT_SIGNING_DEADLINE_DAYS = 14;

    private ContractLifecycleRules() {
    }

    public static LocalDate today() {
        return LocalDate.now(CONTRACT_TIME_ZONE);
    }

    /**
     * Trạng thái ngay khi bên cuối cùng ký xong.
     * - Đã quá expiration date      -> OVERDUE (phải thanh lý)
     * - Chưa tới effective date     -> PENDING_EFFECTIVE
     * - Còn lại                     -> ACTIVE
     */
    public static ContractStatus statusAfterFullySigned(
            LocalDate effectiveDate,
            LocalDate expirationDate,
            LocalDate today
    ) {
        if (isPastExpiration(expirationDate, today)) {
            return ContractStatus.OVERDUE;
        }
        if (effectiveDate != null && effectiveDate.isAfter(today)) {
            return ContractStatus.PENDING_EFFECTIVE;
        }
        return ContractStatus.ACTIVE;
    }

    /**
     * Hạn ký mặc định = today + N ngày, nhưng không vượt quá expiration date
     * (hợp đồng đã hết hạn thì ký cũng vô nghĩa).
     */
    public static LocalDate defaultSigningDeadline(
            LocalDate today,
            LocalDate expirationDate,
            int deadlineDays
    ) {
        LocalDate deadline = today.plusDays(Math.max(deadlineDays, 1));
        if (expirationDate != null && expirationDate.isBefore(deadline)) {
            return expirationDate.isBefore(today) ? today : expirationDate;
        }
        return deadline;
    }

    // Hạn ký là ngày cuối cùng còn được ký (inclusive)
    public static boolean isSigningDeadlinePassed(LocalDate signingDeadline, LocalDate today) {
        return signingDeadline != null && today.isAfter(signingDeadline);
    }

    // Expiration date là ngày cuối cùng hợp đồng còn hiệu lực (inclusive)
    public static boolean isPastExpiration(LocalDate expirationDate, LocalDate today) {
        return expirationDate != null && today.isAfter(expirationDate);
    }
}
