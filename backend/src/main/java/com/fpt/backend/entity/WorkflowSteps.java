package com.fpt.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workflow_steps")
public class WorkflowSteps extends BaseEntity{
    @Column(name = "step_order")
    private Integer stepOrder;
    @Column(name = "step_name")
    private String stepName;
    @Column(name = "step_type")
    private String stepType;
    @Column(name = "is_required")
    private Boolean isRequired;
    @Column(name = "can_reject")
    private Boolean canReject;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    ///
    // COntractApprovals
    @OneToMany(mappedBy = "workflowSteps")
    private List<ContractApprovals> contractApprovals;
    //- workflow_id
    //- approver_role_id
    //- approver_user_id
}
