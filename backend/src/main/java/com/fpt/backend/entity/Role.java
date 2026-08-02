package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "role")
public class Role extends BaseEntity{
    @Column(name = "role_name")
    private String roleName;

    /// Relation
    // user role
    @OneToMany(mappedBy = "role")
    private List<UserRole> userRoles;
}
