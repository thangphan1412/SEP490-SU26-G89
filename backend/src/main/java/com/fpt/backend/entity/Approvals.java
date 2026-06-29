package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "approvals")
public class Approvals extends BaseEntity {
    @Column(name = "approval_level")
    private String approvalLevel;
    @Column(name = "approval_status")
    private String approvalStatus;
    @Column(name = "approval_at")
    private LocalDate approvalAt;

    /// Relation
    //proposal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id")
    private Proposals proposal;
}
