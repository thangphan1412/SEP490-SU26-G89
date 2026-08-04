package com.fpt.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "permission_action_catalog",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_permission_action_code",
                columnNames = "action_code"
        )
)
public class PermissionAction extends BaseEntity {
    @Column(
            name = "action_code",
            nullable = false,
            length = 50,
            columnDefinition = "nvarchar(50)"
    )
    private String actionCode;

    @Column(
            name = "action_name",
            nullable = false,
            length = 100,
            columnDefinition = "nvarchar(100)"
    )
    private String actionName;

    @Column(
            name = "resource_code",
            nullable = false,
            length = 30,
            columnDefinition = "nvarchar(30)"
    )
    private String resourceCode;

    @Column(
            name = "action_description",
            length = 255,
            columnDefinition = "nvarchar(255)"
    )
    private String actionDescription;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "action_is_active", nullable = false)
    private Boolean status;
}
