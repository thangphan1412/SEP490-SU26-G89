package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractTransitionRequest;
import com.fpt.backend.dto.response.contract.ContractResponse;
import com.fpt.backend.entity.ContractStatusHistory;
import com.fpt.backend.entity.ContractWorkflowStepInstance;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Role;
import com.fpt.backend.entity.UserRole;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.ContractWorkflowActionType;
import com.fpt.backend.enums.ContractWorkflowStepState;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.contract.ContractAttributeValueRepository;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractStatusHistoryRepository;
import com.fpt.backend.repository.contract.ContractWorkflowStepInstanceRepository;
import com.fpt.backend.util.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContractServiceImplLifecycleActionTest {
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private ContractStatusHistoryRepository contractStatusHistoryRepository;
    @Mock
    private ContractWorkflowStepInstanceRepository workflowStepRepository;
    @Mock
    private ContractAttributeValueRepository contractAttributeValueRepository;
    @Mock
    private ContractDocumentRenderer documentRenderer;
    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private ContractServiceImpl contractService;

    private Users owner;
    private Contracts contract;

    @BeforeEach
    void setUp() {
        owner = user("Owner", "EMPLOYEE");
        contract = new Contracts();
        contract.setId(UUID.randomUUID());
        contract.setContractNumber("HD-001");
        contract.setContractTitle("Service contract");
        contract.setContractCreatedByUser(owner);
        contract.setEffectiveDate(LocalDate.now().minusDays(10));
        contract.setExpirationDate(LocalDate.now().plusDays(30));

        when(currentUser.getCurrentUser()).thenReturn(owner);
        when(contractRepository.findById(contract.getId())).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contracts.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentRenderer.render(any(), anyList(), anyMap()))
                .thenReturn(new ContractDocumentRenderer.RenderedDocument("content", null, null, null, null));
    }

    @Test
    void ownerCanSettleActiveContract() {
        contract.setContractStatus("ACTIVE");

        ContractResponse response = contractService.transitionContract(
                contract.getId(), request("SETTLE", "Settlement minutes BB-01", null)
        );

        assertThat(response.contractStatus()).isEqualTo("SETTLED");
        assertThat(response.settledAt()).isNotNull();
        assertThat(response.settledByName()).isEqualTo("Owner Test");
        assertThat(response.settlementNote()).isEqualTo("Settlement minutes BB-01");
        assertThat(response.workflowRuntime().availableActions()).doesNotContain("SETTLE", "CANCEL");
        ArgumentCaptor<ContractStatusHistory> history = ArgumentCaptor.forClass(ContractStatusHistory.class);
        verify(contractStatusHistoryRepository).save(history.capture());
        assertThat(history.getValue().getFromStatus()).isEqualTo("ACTIVE");
        assertThat(history.getValue().getToStatus()).isEqualTo("SETTLED");
        assertThat(history.getValue().getAction()).isEqualTo("SETTLE");
    }

    @Test
    void overdueContractCanBeSettled() {
        contract.setContractStatus("OVERDUE");
        contract.setExpirationDate(LocalDate.now().minusDays(3));

        ContractResponse response = contractService.transitionContract(
                contract.getId(), request("SETTLE", "Late settlement", null)
        );

        assertThat(response.contractStatus()).isEqualTo("SETTLED");
    }

    @Test
    void activeContractOffersSettleButNotCancel() {
        contract.setContractStatus("ACTIVE");

        ContractResponse response = contractService.getContractById(contract.getId());

        assertThat(response.workflowRuntime().availableActions())
                .contains("SETTLE")
                .doesNotContain("CANCEL", "EXTEND_SIGNING_DEADLINE");
    }

    @Test
    void settlementRequiresNote() {
        contract.setContractStatus("ACTIVE");

        assertThatThrownBy(() -> contractService.transitionContract(
                contract.getId(), request("SETTLE", " ", null)
        )).isInstanceOf(BadHttpException.class).hasMessage("A settlement note is required");
    }

    @Test
    void partiallySignedContractCannotBeSettled() {
        contract.setContractStatus("PENDING_SIGNATURE");

        assertThatThrownBy(() -> contractService.transitionContract(
                contract.getId(), request("SETTLE", "note", null)
        )).isInstanceOf(BadHttpException.class)
                .hasMessageContaining("Only a contract signed by all parties can be settled");
    }

    @Test
    void anotherEmployeeCannotSettleStandaloneContract() {
        contract.setContractStatus("ACTIVE");
        when(currentUser.getCurrentUser()).thenReturn(user("Other", "EMPLOYEE"));

        assertThatThrownBy(() -> contractService.transitionContract(
                contract.getId(), request("SETTLE", "note", null)
        )).isInstanceOf(ResponseStatusException.class);
        verify(contractStatusHistoryRepository, never()).save(any());
    }

    @Test
    void ceoCanSettleAnyContract() {
        contract.setContractStatus("ACTIVE");
        when(currentUser.getCurrentUser()).thenReturn(user("Ceo", "CEO"));

        ContractResponse response = contractService.transitionContract(
                contract.getId(), request("SETTLE", "Settled by CEO", null)
        );

        assertThat(response.contractStatus()).isEqualTo("SETTLED");
    }

    @Test
    void fullySignedContractCannotBeCancelled() {
        contract.setContractStatus("ACTIVE");
        when(workflowStepRepository.existsByContractId(contract.getId())).thenReturn(true);

        assertThatThrownBy(() -> contractService.transitionContract(
                contract.getId(), request("CANCEL", "reason", null)
        )).isInstanceOf(BadHttpException.class)
                .hasMessageContaining("cannot be cancelled");
    }

    @Test
    void ownerCanExtendSigningDeadlineWithinExpirationDate() {
        contract.setContractStatus("PENDING_SIGNATURE");
        contract.setSigningDeadline(LocalDate.now().plusDays(2));
        LocalDate newDeadline = LocalDate.now().plusDays(10);

        ContractResponse response = contractService.transitionContract(
                contract.getId(), request("EXTEND_SIGNING_DEADLINE", "Partner is travelling", newDeadline)
        );

        assertThat(response.contractStatus()).isEqualTo("PENDING_SIGNATURE");
        assertThat(response.signingDeadline()).isEqualTo(newDeadline);
        ArgumentCaptor<ContractStatusHistory> history = ArgumentCaptor.forClass(ContractStatusHistory.class);
        verify(contractStatusHistoryRepository).save(history.capture());
        assertThat(history.getValue().getAction()).isEqualTo("EXTEND_SIGNING_DEADLINE");
    }

    @Test
    void signingDeadlineCannotGoPastExpirationDate() {
        contract.setContractStatus("PENDING_SIGNATURE");
        contract.setSigningDeadline(LocalDate.now().plusDays(2));

        assertThatThrownBy(() -> contractService.transitionContract(
                contract.getId(),
                request("EXTEND_SIGNING_DEADLINE", "reason", contract.getExpirationDate().plusDays(1))
        )).isInstanceOf(BadHttpException.class)
                .hasMessageContaining("cannot be after the contract expiration date");
    }

    @Test
    void signingAfterDeadlineIsRejected() {
        contract.setContractStatus("PENDING_SIGNATURE");
        contract.setSigningDeadline(LocalDate.now().minusDays(1));
        ContractWorkflowStepInstance signStep = new ContractWorkflowStepInstance();
        signStep.setId(UUID.randomUUID());
        signStep.setActionType(ContractWorkflowActionType.SIGN);
        signStep.setStatus(ContractWorkflowStepState.PENDING);
        signStep.setAssignedUser(owner);
        signStep.setRequiredRoleCode("EMPLOYEE");
        when(workflowStepRepository.existsByContractId(contract.getId())).thenReturn(true);
        when(workflowStepRepository.findFirstByContractIdAndStatusOrderByStepOrderAsc(
                contract.getId(), ContractWorkflowStepState.PENDING
        )).thenReturn(Optional.of(signStep));

        assertThatThrownBy(() -> contractService.transitionContract(
                contract.getId(), request("COMPLETE_STEP", null, null)
        )).isInstanceOf(BadHttpException.class)
                .hasMessageContaining("signing deadline");
        assertThat(signStep.getStatus()).isEqualTo(ContractWorkflowStepState.PENDING);
    }

    private static ContractTransitionRequest request(String action, String comment, LocalDate deadline) {
        return new ContractTransitionRequest(action, null, null, comment, null, null, null, deadline);
    }

    private static Users user(String firstName, String roleCode) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setRoleCode(roleCode);
        role.setRoleName(roleCode);
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setFirstName(firstName);
        user.setLastName("Test");
        user.setEmail(firstName.toLowerCase() + "@example.com");
        user.setDob("2000-01-01");
        user.setUserRoles(List.of(UserRole.builder().user(user).role(role).build()));
        return user;
    }
}
