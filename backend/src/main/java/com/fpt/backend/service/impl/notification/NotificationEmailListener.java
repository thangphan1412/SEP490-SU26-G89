package com.fpt.backend.service.impl.notification;

import com.fpt.backend.mail.EmailService;
import com.fpt.backend.mail.MessageInfor;
import com.fpt.backend.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEmailListener {
    private static final ZoneId NOTIFICATION_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final EmailService emailService;
    private final NotificationRepository notificationRepository;

    // Chỉ gửi mail sau khi thông báo + đổi trạng thái hợp đồng đã commit
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(NotificationEmailEvent event) {
        if (event.recipientEmail() == null || event.recipientEmail().isBlank()) {
            log.warn("Notification email skipped because the recipient email is empty");
            return;
        }
        try {
            emailService.sendEmail(new MessageInfor(
                    event.recipientEmail(),
                    event.subject(),
                    event.body()
            ));
        } catch (RuntimeException exception) {
            log.error("Unable to send notification email to {}", event.recipientEmail(), exception);
            return;
        }
        if (event.notificationId() != null) {
            notificationRepository.markEmailSent(
                    event.notificationId(),
                    LocalDateTime.now(NOTIFICATION_TIME_ZONE)
            );
        }
    }
}
