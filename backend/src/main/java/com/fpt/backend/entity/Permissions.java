package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "permissions")
public class Permissions extends BaseEntity {
    @Column(name = "permission_name", columnDefinition = "nvarchar(50)")
    private String permissionName;
    @Column(
            name = "permission_code",
            columnDefinition = "nvarchar(50)",
            unique = true
    )
    private String permissionCode;
    @Column(name = "permission_module", columnDefinition = "nvarchar(255)")
    private String permissionModule;

    /// Relation
    // userpermistion
    @OneToMany(mappedBy = "permission", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<UserPermission> userPermissions;
    // Project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Projects project;

    @Column(name = "permission_description", columnDefinition = "nvarchar(255)")
    private String permissionDescription;

    @Column(name = "permission_is_active")
    private Boolean status;

    @Column(name = "permission_created_at")
    private LocalDateTime createdAt;
    
}
