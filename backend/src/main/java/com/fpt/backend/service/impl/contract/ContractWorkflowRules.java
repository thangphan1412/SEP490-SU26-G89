package com.fpt.backend.service.impl.contract;

import com.fpt.backend.enums.ContractStatus;
import com.fpt.backend.enums.ContractWorkflowActionType;

import java.util.List;

final class ContractWorkflowRules {

    private ContractWorkflowRules() {
    }

    static List<String> requiredPermissions(ContractWorkflowActionType actionType) {
        return switch (actionType) {
            case CREATE -> List.of(
                    ContractProjectActions.VIEW,
                    ContractProjectActions.CREATE,
                    ContractProjectActions.SUBMIT
            );
            case APPROVE -> List.of(
                    ContractProjectActions.VIEW,
                    ContractProjectActions.APPROVE
            );
            case SIGN -> List.of(
                    ContractProjectActions.VIEW,
                    ContractProjectActions.SIGN
            );
            case APPROVE_AND_SIGN -> List.of(
                    ContractProjectActions.VIEW,
                    ContractProjectActions.APPROVE,
                    ContractProjectActions.SIGN
            );
        };
    }

    static ContractStatus pendingStatus(ContractWorkflowActionType actionType) {
        return switch (actionType) {
            case CREATE -> ContractStatus.NEW;
            case APPROVE -> ContractStatus.PENDING_APPROVAL;
            case SIGN, APPROVE_AND_SIGN -> ContractStatus.PENDING_SIGNATURE;
        };
    }

    static String historyAction(ContractWorkflowActionType actionType) {
        return switch (actionType) {
            case CREATE -> "SUBMIT";
            case APPROVE -> "APPROVE";
            case SIGN -> "SIGN";
            case APPROVE_AND_SIGN -> "APPROVE_AND_SIGN";
        };
    }
}
