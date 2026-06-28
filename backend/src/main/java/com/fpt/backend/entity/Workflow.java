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
    @Column(name = "step_oder")
    private String stepOder;
    @Column(name = "approver_role")
    private String approverRole;
    @Column(name = "is_required")
    private String isRequired;

    /// Relation
    //project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Projects project;
}
