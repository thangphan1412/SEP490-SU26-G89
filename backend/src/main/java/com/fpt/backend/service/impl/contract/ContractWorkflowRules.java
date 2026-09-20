package com.fpt.backend.service.impl.contract;

import com.fpt.backend.enums.ContractStatus;
import com.fpt.backend.enums.ContractWorkflowActionType;

import java.util.List;

public final class ContractWorkflowRules {

    private ContractWorkflowRules() {
    }

    public static List<String> requiredPermissions(ContractWorkflowActionType actionType) {
        return switch (actionType) {
            case CREATE -> List.of(
                    ContractProjectActions.VIEW,
                    ContractProjectActions.CREATE,
                    ContractProjectActions.SUBMIT
            );
            case APPROVE, APPROVE_AND_GENERATE_PDF, APPROVE_AND_SIGN -> List.of(
                    ContractProjectActions.VIEW,
                    ContractProjectActions.APPROVE
            );
            case SIGN -> List.of(
                    ContractProjectActions.VIEW,
                    ContractProjectActions.SIGN
            );
        };
    }

    public static ContractStatus pendingStatus(ContractWorkflowActionType actionType) {
        return switch (actionType) {
            case CREATE -> ContractStatus.NEW;
            case APPROVE, APPROVE_AND_GENERATE_PDF, APPROVE_AND_SIGN ->
                    ContractStatus.PENDING_APPROVAL;
            case SIGN -> ContractStatus.PENDING_SIGNATURE;
        };
    }

    public static String historyAction(ContractWorkflowActionType actionType) {
        return switch (actionType) {
            case CREATE -> "SUBMIT";
            case APPROVE -> "APPROVE";
            case SIGN -> "SIGN";
            case APPROVE_AND_GENERATE_PDF, APPROVE_AND_SIGN ->
                    "APPROVE_AND_GENERATE_PDF";
        };
    }
}
