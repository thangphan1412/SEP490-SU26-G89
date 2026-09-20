package com.fpt.backend.service.impl.contract;

import com.fpt.backend.enums.ContractStatus;
import com.fpt.backend.enums.ContractWorkflowActionType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContractWorkflowRulesTest {

    @Test
    void ceoApprovalGeneratesPdfWithoutRequiringSignature() {
        ContractWorkflowActionType actionType =
                ContractWorkflowActionType.APPROVE_AND_GENERATE_PDF;

        assertThat(actionType.requiresSignature()).isFalse();
        assertThat(actionType.generatesApprovedPdf()).isTrue();
        assertThat(ContractWorkflowRules.requiredPermissions(actionType))
                .containsExactly(
                        ContractProjectActions.VIEW,
                        ContractProjectActions.APPROVE
                );
        assertThat(ContractWorkflowRules.pendingStatus(actionType))
                .isEqualTo(ContractStatus.PENDING_APPROVAL);
        assertThat(ContractWorkflowRules.historyAction(actionType))
                .isEqualTo("APPROVE_AND_GENERATE_PDF");
    }

    @Test
    void signatureRemainsASeparateWorkflowAction() {
        ContractWorkflowActionType actionType =
                ContractWorkflowActionType.SIGN;

        assertThat(actionType.requiresSignature()).isTrue();
        assertThat(actionType.generatesApprovedPdf()).isFalse();
        assertThat(ContractWorkflowRules.requiredPermissions(actionType))
                .containsExactly(
                        ContractProjectActions.VIEW,
                        ContractProjectActions.SIGN
                );
        assertThat(ContractWorkflowRules.pendingStatus(actionType))
                .isEqualTo(ContractStatus.PENDING_SIGNATURE);
    }
}
