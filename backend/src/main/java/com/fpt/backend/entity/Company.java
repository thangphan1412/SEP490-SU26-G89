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
public class Company extends BaseEntity {

    @Column(name = "company_name", columnDefinition = "nvarchar(max)")
    private String companyName;

    @Column(name = "email", columnDefinition = "nvarchar(max)")
    private String email;

    @Column(name = "registered_address", columnDefinition = "nvarchar(max)")
    private String registeredAddress;

    // Phân biệt công ty của mình (true) và công ty đối tác (false)
    @Column(name = "is_internal")
    private Boolean isInternal;

    /// Relation: 1 Công ty có nhiều User
    @OneToMany(mappedBy = "company")
    private List<Users> users;
}