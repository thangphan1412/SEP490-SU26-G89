package com.fpt.backend.entity;

import com.fpt.backend.enums.ContractWorkflowActionType;
import com.fpt.backend.enums.ContractWorkflowStepState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "contract_workflow_step_instances",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_contract_runtime_step_order",
                columnNames = {"contract_id", "step_order"}
        )
)
@SuppressWarnings("JpaDataSourceORMInspection")
public class ContractWorkflowStepInstance extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_contract_runtime_step_contract")
    )
    private Contracts contract;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "step_definition_id",
            foreignKey = @ForeignKey(name = "FK_contract_runtime_step_definition")
    )
    private ContractTypeWorkflowStep stepDefinition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "assigned_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_contract_runtime_step_user")
    )
    private Users assignedUser;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "step_name", nullable = false, columnDefinition = "nvarchar(150)")
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ContractWorkflowActionType actionType;

    @Column(name = "required_role_code", nullable = false, length = 60)
    private String requiredRoleCode;

    @Column(name = "is_required", nullable = false)
    private Boolean required;

    @Column(name = "can_reject", nullable = false)
    private Boolean canReject;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_status", nullable = false, length = 30)
    private ContractWorkflowStepState status;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completion_comment", columnDefinition = "nvarchar(1000)")
    private String comment;
}
