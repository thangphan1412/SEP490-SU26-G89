package com.fpt.backend.entity;

import com.fpt.backend.enums.ContractWorkflowActionType;
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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "contract_type_workflow_steps",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_contract_workflow_step_order",
                columnNames = {"workflow_id", "step_order"}
        )
)
@SuppressWarnings("JpaDataSourceORMInspection")
public class ContractTypeWorkflowStep extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "workflow_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_contract_workflow_step_workflow")
    )
    private ContractTypeWorkflow workflow;

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
}
