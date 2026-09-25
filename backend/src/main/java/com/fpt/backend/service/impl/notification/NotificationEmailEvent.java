package com.fpt.backend.service.impl.notification;

import java.util.UUID;

public record NotificationEmailEvent(
        UUID notificationId,
        String recipientEmail,
        String subject,
        String body
) {
}
