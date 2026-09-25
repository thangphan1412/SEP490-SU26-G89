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
public class ContractExpirationEmailListener {
    private static final ZoneId NOTIFICATION_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final EmailService emailService;
    private final NotificationRepository notificationRepository;

    // Chỉ gửi mail sau khi thông báo đã commit vào DB
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(ContractExpirationEmailEvent event) {
        if (event.recipientEmail() == null || event.recipientEmail().isBlank()) {
            log.warn("Contract expiration email skipped because the CEO email is empty");
            return;
        }
        try {
            emailService.sendEmail(new MessageInfor(
                    event.recipientEmail(),
                    "Contract expiring soon - " + event.contractNumber(),
                    buildBody(event)
            ));
        } catch (RuntimeException exception) {
            // emailSent vẫn false -> lần chạy scheduler sau sẽ gửi lại
            log.error("Unable to send contract expiration email to {}", event.recipientEmail(), exception);
            return;
        }
        if (event.notificationId() != null) {
            notificationRepository.markEmailSent(
                    event.notificationId(),
                    LocalDateTime.now(NOTIFICATION_TIME_ZONE)
            );
        }
    }

    static String buildBody(ContractExpirationEmailEvent event) {
        return "Dear " + event.recipientName() + ",\n\n"
                + "The following contract is about to expire ("
                + ContractExpirationReminderService.daysRemainingText(event.daysRemaining()) + ").\n\n"
                + "Contract number: " + event.contractNumber() + "\n"
                + "Contract title: " + event.contractTitle() + "\n"
                + "Effective date: " + (event.effectiveDate() == null ? "-" : event.effectiveDate()) + "\n"
                + "Expiration date: " + event.expirationDate() + "\n\n"
                + "Please review the contract and decide whether it should be renewed or allowed to end.";
    }
}
