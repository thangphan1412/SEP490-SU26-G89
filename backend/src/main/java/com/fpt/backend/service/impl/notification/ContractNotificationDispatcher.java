package com.fpt.backend.service.impl.notification;

import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Notifications;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.NotificationType;
import com.fpt.backend.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Tạo thông báo in-app (bảng notifications) và gửi mail tương ứng sau khi commit.
 * Phải được gọi bên trong một transaction.
 */
@Component
@RequiredArgsConstructor
public class ContractNotificationDispatcher {
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * @return true nếu tạo mới, false nếu referenceKey đã gửi cho user này rồi
     */
    public boolean notify(
            Users recipient,
            Contracts contract,
            NotificationType type,
            String referenceKey,
            String title,
            String content,
            LocalDateTime now
    ) {
        if (recipient == null) {
            return false;
        }
        if (referenceKey != null
                && notificationRepository.existsByUserIdAndReferenceKey(recipient.getId(), referenceKey)) {
            return false;
        }
        Notifications notification = notificationRepository.save(Notifications.builder()
                .title(title)
                .content(content)
                .type(type.name())
                .isRead(false)
                .createAt(now)
                .referenceKey(referenceKey)
                .emailSent(false)
                .user(recipient)
                .contract(contract)
                .build());
        eventPublisher.publishEvent(new NotificationEmailEvent(
                notification.getId(),
                recipient.getEmail(),
                title,
                "Dear " + displayName(recipient) + ",\n\n" + content
        ));
        return true;
    }

    public static String displayName(Users user) {
        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return fullName.isEmpty() ? user.getEmail() : fullName;
    }
}
