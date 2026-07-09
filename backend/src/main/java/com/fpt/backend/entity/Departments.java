package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "departments")
public class Departments extends BaseEntity{
    @Column(name = "department_name")
    private String departmentName;
    @Column(name = "department_code")
    private String departmentCode;
    @Column(name = "department_create_at")
    private LocalDate departmentCreateAt;
    @Column(name = "department_status")
    private String departmentStatus;

    /// Relation
    //User
    @OneToMany(mappedBy = "department")
    private List<Users>  users;
    //Proposal
    @OneToMany(mappedBy = "department")
    private List<Proposals> proposals;

    // comapy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
}
