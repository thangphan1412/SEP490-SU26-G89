package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.catalina.User;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "permissions")
public class Permissions extends BaseEntity{
    @Column(name = "permission_name")
    private String permissionName;
    @Column(name = "permission_co   de")
    private String permissionCode;
    @Column(name = "permission_module")
    private String permissionModule;

    /// Relation
    // userpermistion
    @OneToMany(mappedBy = "permission")
    private List<UserPermission> userPermissions;
    //Project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Projects project;

    //role
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;
}
