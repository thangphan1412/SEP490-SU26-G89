package com.fpt.backend.enums;

/**
 * Action codes used by permission checks.
 * Action metadata is stored in permission_action_catalog.
 */
public enum PermissionModule {
    VIEW_TASKS,
    CREATE_TASKS,
    EDIT_TASKS,
    DELETE_TASKS,
    VIEW_DELIVERABLES,
    CREATE_DELIVERABLES,
    EDIT_DELIVERABLES,
    DELETE_DELIVERABLES,
    VIEW_CONTRACTS,
    EDIT_PROJECT,
    EDIT_PHASE,
    MANAGE_MEMBERS;

    public String getActionCode() {
        return name();
    }
}
