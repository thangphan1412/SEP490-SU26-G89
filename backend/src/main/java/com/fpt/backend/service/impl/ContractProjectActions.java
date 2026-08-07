package com.fpt.backend.service.impl;

import java.util.List;

/**
 * Permission action codes owned by the Contract Management module.
 */
public final class ContractProjectActions {
    public static final String VIEW = "VIEW_CONTRACTS";
    public static final String CREATE = "CREATE_CONTRACTS";
    public static final String EDIT = "EDIT_CONTRACTS";
    public static final String DELETE = "DELETE_CONTRACTS";
    public static final String SUBMIT = "SUBMIT_CONTRACTS";
    public static final String APPROVE = "APPROVE_CONTRACTS";
    public static final String SIGN = "SIGN_CONTRACTS";
    public static final String CANCEL = "CANCEL_CONTRACTS";
    public static final String EXPORT = "EXPORT_CONTRACTS";

    private static final String RESOURCE_CODE = "CONTRACT";

    private ContractProjectActions() {
    }

    public static List<Definition> definitions() {
        return List.of(
                new Definition(
                        VIEW,
                        "View contracts",
                        "View contracts assigned through the selected project",
                        40
                ),
                new Definition(
                        CREATE,
                        "Create contracts",
                        "Create a contract for the selected project",
                        41
                ),
                new Definition(
                        EDIT,
                        "Edit contracts",
                        "Edit contracts while they are in NEW status",
                        42
                ),
                new Definition(
                        DELETE,
                        "Delete contracts",
                        "Delete contracts while they are in NEW status",
                        43
                ),
                new Definition(
                        SUBMIT,
                        "Submit contracts",
                        "Submit NEW contracts for internal approval",
                        44
                ),
                new Definition(
                        APPROVE,
                        "Approve contracts",
                        "Approve or reject contracts during internal review",
                        45
                ),
                new Definition(
                        SIGN,
                        "Sign contracts",
                        "Sign or reject contracts at the applicable signature stage",
                        46
                ),
                new Definition(
                        CANCEL,
                        "Cancel contracts",
                        "Cancel non-terminal contracts when the workflow permits it",
                        47
                ),
                new Definition(
                        EXPORT,
                        "Export contract PDF",
                        "Export the completed PDF after both parties have signed",
                        48
                )
        );
    }

    public record Definition(
            String code,
            String name,
            String description,
            int displayOrder
    ) {
        public String resourceCode() {
            return RESOURCE_CODE;
        }
    }
}
