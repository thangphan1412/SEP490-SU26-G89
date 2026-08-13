package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "projects")
public class Projects extends BaseEntity {
    @Column(name = "project_code", unique = true)
    private String projectCode;
    @Column(name = "project_name", columnDefinition = "nvarchar(50)")
    private String projectName;
    @Column(name = "project_description", columnDefinition = "nvarchar(255)")
    private String projectDescription;
    @Column(name = "project_status", columnDefinition = "nvarchar(50)")
    private String projectStatus;
    @Column(name = "project_start_date")
    private LocalDate projectStartDate;
    @Column(name = "project_end_date")
    private LocalDate projectEndDate;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_created_by_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_projects_created_by_user")
    )
    private Users projectCreatedBy;
    @Column(name = "project_created_at", columnDefinition = "nvarchar(50)")
    private String projectCreatedAt;

    /// Relation
    // permission
    @OneToMany(
            mappedBy = "project",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<Permissions> permission;
    // Logs
    @OneToMany(mappedBy = "project")
    private List<ActivityLog> activityLog;
    // Workflow
    @OneToMany(mappedBy = "project")
    private List<Workflow> workflow;
    // Contract
    @OneToMany(mappedBy = "project")
    private List<Contracts> contract;
    // proposal
    @OneToMany(mappedBy = "project")
    private List<Proposals> proposals;
    // projectmember
    @OneToMany(mappedBy = "project")
    private List<ProjectMember> projectMembers;
    // timeline
    @OneToMany(mappedBy = "project")
    private List<Timeline> timelines;
}
