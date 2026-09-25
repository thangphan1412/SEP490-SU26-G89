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
@Table(
        name = "approvals",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_approvals_proposal_level",
                columnNames = {"proposal_id", "approval_level"}
        )
)
public class Approvals extends BaseEntity {
    @Column(name = "approval_level", columnDefinition = "nvarchar(255)")
    private String approvalLevel;
    @Column(name = "approval_status", columnDefinition = "nvarchar(max)")
    private String approvalStatus;
    @Column(name = "approval_at")
    private LocalDate approvalAt;

    /// Relation
    //proposal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id")
    private Proposals proposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private Users approvedBy;
}
