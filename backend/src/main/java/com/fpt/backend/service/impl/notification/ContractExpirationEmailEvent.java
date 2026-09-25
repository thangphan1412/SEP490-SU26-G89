package com.fpt.backend.service.impl.notification;

import java.time.LocalDate;
import java.util.UUID;

public record ContractExpirationEmailEvent(
        UUID notificationId,
        String recipientName,
        String recipientEmail,
        String contractNumber,
        String contractTitle,
        LocalDate effectiveDate,
        LocalDate expirationDate,
        long daysRemaining
) {
}
