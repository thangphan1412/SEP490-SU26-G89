package com.fpt.backend.entity;

import com.fpt.backend.enums.DepartmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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
    @Column(name = "department_create_at", nullable = false)
    private LocalDateTime departmentCreatedAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "department_status", nullable = false, length = 20)
    private DepartmentStatus departmentStatus;

    /// Relation
    //User
    @OneToMany(mappedBy = "department")
    private List<Users>  users;
    //Proposal
    @OneToMany(mappedBy = "department")
    private List<Proposals> proposals;

    // Company
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
}
