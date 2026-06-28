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
    @Column(name = "project_code")
    private String projectCode;
    @Column(name = "project_name")
    private String projectName;
    @Column(name = "project_description")
    private String projectDescription;
    @Column(name = "project_status")
    private String projectStatus;
    @Column(name = "project_start_date")
    private LocalDate projectStartDate;
    @Column(name = "project_end_date")
    private LocalDate projectEndDate;
    @Column(name = "project_creat-by")
    private String projectCreatedBy;
    @Column(name = "project_created_at")
    private String projectCreatedAt;

    /// Relation
    // permission
    @OneToMany(mappedBy = "project")
    private List<Permissions> permission;
    //Logs
    @OneToMany(mappedBy = "project")
    private List<ActivityLog> activityLog;
    //Workflow
    @OneToMany(mappedBy = "project")
    private List<Workflow>  workflow;
    //Contract
    @OneToMany(mappedBy = "project")
    private List<Contracts> contract;
    //proposal
    @OneToMany(mappedBy = "project")
    private List<Proposals> proposals;
}
