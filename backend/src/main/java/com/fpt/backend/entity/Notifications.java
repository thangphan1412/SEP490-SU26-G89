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
@Table(name = "notifications")
public class Notifications extends BaseEntity{
    @Column(name = "notifications_title")
    private String title;
    @Column(name = "notifications_content")
    private String content;
    @Column(name = "notifications_type")
    private String type;
    @Column(name = "notifications_is_read")
    private Boolean isRead;
    @Column(name = "notifications_create_at")
    private LocalDateTime createAt;

    /// Relation
    //user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;
}
