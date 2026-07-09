package com.fpt.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "Company")
public class Company extends BaseEntity{
    @Column(name = "company_name")
    private String companyName;
    @Column(name = "company_code")
    private String companyCode;

    /// Relation
    //Department
    @OneToMany(mappedBy = "company")
    private List<Departments> departments;
}
