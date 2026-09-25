package com.fpt.backend.repository.notification;

import com.fpt.backend.entity.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notifications, UUID> {
    boolean existsByUserIdAndReferenceKey(UUID userId, String referenceKey);

    // Thông báo hợp đồng chưa gửi mail được (lỗi SMTP...) và hợp đồng vẫn chưa hết hạn
    @Query("""
            SELECT notification
            FROM Notifications notification
            JOIN FETCH notification.user
            JOIN FETCH notification.contract contract
            WHERE notification.type = :type
                AND (notification.emailSent IS NULL OR notification.emailSent = false)
                AND UPPER(COALESCE(contract.contractStatus, '')) IN :statuses
                AND contract.expirationDate >= :today
            """)
    List<Notifications> findUnsentContractNotifications(
            @Param("type") String type,
            @Param("statuses") List<String> statuses,
            @Param("today") LocalDate today
    );

    @Modifying
    @Query("""
            UPDATE Notifications notification
            SET notification.emailSent = true,
                notification.emailSentAt = :sentAt
            WHERE notification.id = :notificationId
            """)
    int markEmailSent(
            @Param("notificationId") UUID notificationId,
            @Param("sentAt") LocalDateTime sentAt
    );
}
