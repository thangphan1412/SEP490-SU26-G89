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

    /**
     * These values are used only to seed the system action catalog.
     * Permission configuration is read from the database at runtime.
     */
    public enum DefaultAction {
        VIEW_TASKS(
                "Allow View Tasks",
                "TASK",
                "View tasks in the permitted work scope.",
                10
        ),
        CREATE_TASKS(
                "Allow Create Tasks",
                "TASK",
                "Create tasks in a project phase.",
                20
        ),
        EDIT_TASKS(
                "Allow Edit Tasks",
                "TASK",
                "Edit tasks in the permitted work scope.",
                30
        ),
        DELETE_TASKS(
                "Allow Delete Tasks",
                "TASK",
                "Delete tasks in the permitted work scope.",
                40
        ),
        VIEW_DELIVERABLES(
                "Allow View Deliverables",
                "DELIVERABLE",
                "View project deliverables.",
                50
        ),
        CREATE_DELIVERABLES(
                "Allow Create Deliverables",
                "DELIVERABLE",
                "Create project deliverables.",
                60
        ),
        EDIT_DELIVERABLES(
                "Allow Edit Deliverables",
                "DELIVERABLE",
                "Edit project deliverables.",
                70
        ),
        DELETE_DELIVERABLES(
                "Allow Delete Deliverables",
                "DELIVERABLE",
                "Delete project deliverables.",
                80
        ),
        VIEW_CONTRACTS(
                "Allow View Contracts",
                "CONTRACT",
                "View contracts linked to the project.",
                90
        ),
        EDIT_PROJECT(
                "Allow Edit Project Informations",
                "PROJECT",
                "Edit the project information.",
                100
        ),
        EDIT_PHASE(
                "Allow Edit Phase Information",
                "PHASE",
                "Edit project phase information.",
                110
        ),
        MANAGE_MEMBERS(
                "Allow Manage Project Members",
                "MEMBER",
                "Add, remove, and configure project members.",
                120
        );

        private final String actionName;
        private final String resourceCode;
        private final String description;
        private final int displayOrder;

        DefaultAction(
                String actionName,
                String resourceCode,
                String description,
                int displayOrder) {
            this.actionName = actionName;
            this.resourceCode = resourceCode;
            this.description = description;
            this.displayOrder = displayOrder;
        }

        public String getActionCode() {
            return name();
        }

        public String getActionName() {
            return actionName;
        }

        public String getResourceCode() {
            return resourceCode;
        }

        public String getDescription() {
            return description;
        }

        public int getDisplayOrder() {
            return displayOrder;
        }
    }
}
