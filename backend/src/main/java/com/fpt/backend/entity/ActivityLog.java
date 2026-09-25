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
@Table(name = "activity_logs")
public class ActivityLog extends BaseEntity{
    @Column(name = "entity_type", columnDefinition = "nvarchar(max)")
    private String entityType;
    @Column(name = "entity_id", columnDefinition = "nvarchar(max)")
    private String entityId;
    @Column(name = "action", columnDefinition = "nvarchar(max)")
    private String action;
    @Column(name = "description", columnDefinition = "nvarchar(max)")
    private String description;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /// Relation
    //User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;
    //project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Projects project;
}
