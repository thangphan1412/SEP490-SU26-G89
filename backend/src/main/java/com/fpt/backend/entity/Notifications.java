package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(
        name = "notifications",
        indexes = @Index(
                name = "IX_notifications_reference_key",
                columnList = "notifications_reference_key"
        )
)
public class Notifications extends BaseEntity{
    @Column(name = "notifications_title", columnDefinition = "nvarchar(max)")
    private String title;
    @Column(name = "notifications_content", columnDefinition = "nvarchar(max)")
    private String content;
    // Giá trị của NotificationType, ví dụ CONTRACT_EXPIRING_SOON
    @Column(name = "notifications_type", columnDefinition = "nvarchar(max)")
    private String type;
    @Column(name = "notifications_is_read")
    private Boolean isRead;
    @Column(name = "notifications_create_at")
    private LocalDateTime createAt;
    // Khóa chống gửi trùng: cùng user + cùng referenceKey chỉ tạo một thông báo
    @Column(name = "notifications_reference_key", columnDefinition = "nvarchar(255)")
    private String referenceKey;
    @Column(name = "notifications_email_sent")
    private Boolean emailSent;
    @Column(name = "notifications_email_sent_at")
    private LocalDateTime emailSentAt;

    /// Relation
    //user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;
    // Hợp đồng liên quan (nullable với thông báo không gắn hợp đồng)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_id",
            foreignKey = @ForeignKey(name = "FK_notifications_contract")
    )
    private Contracts contract;
}
