package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    @Enumerated(EnumType.STRING)
    @Column(
            name = "permission_work_scope",
            nullable = false,
            length = 20,
            columnDefinition = "nvarchar(20)"
    )
    @Builder.Default
    private WorkScope workScope = WorkScope.FULL;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "permission_action_mapping",
            joinColumns = @JoinColumn(
                    name = "permission_id",
                    foreignKey = @ForeignKey(
                            name = "FK_permission_action_permission"
                    )
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "action_id",
                    foreignKey = @ForeignKey(
                            name = "FK_permission_action_catalog"
                    )
            ),
            uniqueConstraints = @UniqueConstraint(
                    name = "UK_permission_action_mapping",
                    columnNames = {"permission_id", "action_id"}
            )
    )
    @OrderBy("displayOrder ASC, actionCode ASC")
    @Builder.Default
    private Set<PermissionAction> actions = new LinkedHashSet<>();

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

    public enum WorkScope {
        OWN,
        FULL
    }
}
