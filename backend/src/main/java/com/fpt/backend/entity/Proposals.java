package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "proposals")
public class Proposals extends BaseEntity {
    @Column(name = "proposal_code")
    private String proposalCode;
    @Column(name = "proposal_title")
    private String title;
    @Column(name = "proposal_description")
    private String description;
    @Column(name = "proposal_status")
    private String status;
    @Column(name = "proposal_create_at")
    private String createAt;
    @Column(name = "proposal_update_at")
    private String updateAt;

    /// Relation
    //user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;
    //project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Projects project;
    //Department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Departments department;
    //approval
    @OneToMany(mappedBy = "proposal")
    private List<Approvals> approvals;
    //statistics

}
