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
    @Column(name = "workflow_name", columnDefinition = "nvarchar(max)")
    private String workflowName;
    @Column(name = "workflow_step_oder", columnDefinition = "nvarchar(max)")
    private String stepOder;
    @Column(name = "workflow_approver_role", columnDefinition = "nvarchar(max)")
    private String approverRole;
    @Column(name = "workflow_is_required", columnDefinition = "nvarchar(max)")
    private String isRequired;

    /// Relation
    //project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Projects project;
}
