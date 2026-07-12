package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "contract_approvals")
public class ContractApprovals extends BaseEntity{
    @Column(name = "approval_level")
    private String approvalLevel;

    //enum
    @Column(name = "approval_status")
    private String approvalStatus;
    @Column(name = "approval_comment")
    private String approvalComment;
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    ///
//      COntract
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contracts contract;
//- workflow_step_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_id")
    private WorkflowSteps workflowSteps;
//- approver_user_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_user_id")
    private Users user;
}
