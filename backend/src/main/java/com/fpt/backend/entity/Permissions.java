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
@Table(name = "permissions")
public class Permissions extends BaseEntity{
    @Column(name = "permission_name" , columnDefinition = "nvarchar(50)")
    private String permissionName;
    @Column(name = "permission_code" , columnDefinition = "nvarchar(50)")
    private String permissionCode;
    @Column(name = "permission_module" , columnDefinition = "nvarchar(255)")
    private String permissionModule;

    /// Relation
    // userpermistion
    @OneToMany(mappedBy = "permission", cascade = CascadeType.REMOVE, orphanRemoval = true)
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
