package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "workflows")
public class Workflow extends BaseEntity{
    @Column(name = "workflow_name")
    private String workflowName;
    @Column(name = "workflow_step_oder")
    private String stepOder;
    @Column(name = "workflow_approver_role")
    private String approverRole;
    @Column(name = "workflow_is_required")
    private String isRequired;

    /// Relation
    //project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Projects project;
}
