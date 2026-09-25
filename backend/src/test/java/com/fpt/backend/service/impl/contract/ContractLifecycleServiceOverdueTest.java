package com.fpt.backend.service.impl.contract;

import com.fpt.backend.entity.ContractStatusHistory;
import com.fpt.backend.entity.ContractWorkflowStepInstance;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.ContractWorkflowActionType;
import com.fpt.backend.enums.ContractWorkflowStepState;
import com.fpt.backend.enums.NotificationType;
import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractStatusHistoryRepository;
import com.fpt.backend.repository.contract.ContractWorkflowStepInstanceRepository;
import com.fpt.backend.repository.user.UserRepository;
import com.fpt.backend.service.impl.notification.ContractNotificationDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContractLifecycleServiceOverdueTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 25);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 25, 0, 5);

    private ContractRepository contractRepository;
    private ContractStatusHistoryRepository historyRepository;
    private ContractWorkflowStepInstanceRepository stepRepository;
    private UserRepository userRepository;
    private ContractNotificationDispatcher dispatcher;
    private ContractLifecycleService service;

    @BeforeEach
    void setUp() {
        contractRepository = mock(ContractRepository.class);
        historyRepository = mock(ContractStatusHistoryRepository.class);
        stepRepository = mock(ContractWorkflowStepInstanceRepository.class);
        userRepository = mock(UserRepository.class);
        dispatcher = mock(ContractNotificationDispatcher.class);
        service = new ContractLifecycleService(
                contractRepository, historyRepository, stepRepository, userRepository, dispatcher
        );
    }

    @Test
    void activeContractPastExpirationBecomesOverdueAndNotifiesCeoAndCreator() {
        Users ceo = user("ceo@example.com");
        Users creator = user("creator@example.com");
        Contracts contract = contract("ACTIVE");
        contract.setExpirationDate(TODAY.minusDays(1));
        contract.setContractCreatedByUser(creator);
        when(contractRepository.findContractIdsPastExpiration(
                ContractLifecycleService.OVERDUE_CANDIDATE_STATUSES, TODAY
        )).thenReturn(List.of(contract.getId()));
        when(contractRepository.findForSigningById(contract.getId())).thenReturn(Optional.of(contract));
        when(userRepository.findUsersByRoleExcludingStatus("CEO", UserStatus.INACTIVE))
                .thenReturn(List.of(ceo));

        int count = service.markOverdueContracts(TODAY, NOW);

        assertThat(count).isEqualTo(1);
        assertThat(contract.getContractStatus()).isEqualTo("OVERDUE");
        assertThat(contract.getOverdueAt()).isEqualTo(NOW);
        ArgumentCaptor<ContractStatusHistory> history = ArgumentCaptor.forClass(ContractStatusHistory.class);
        verify(historyRepository).save(history.capture());
        assertThat(history.getValue().getFromStatus()).isEqualTo("ACTIVE");
        assertThat(history.getValue().getToStatus()).isEqualTo("OVERDUE");
        assertThat(history.getValue().getAction()).isEqualTo("AUTO_OVERDUE");
        verify(dispatcher).notify(eq(ceo), eq(contract), eq(NotificationType.CONTRACT_OVERDUE),
                anyString(), anyString(), anyString(), eq(NOW));
        verify(dispatcher).notify(eq(creator), eq(contract), eq(NotificationType.CONTRACT_OVERDUE),
                anyString(), anyString(), anyString(), eq(NOW));
    }

    @Test
    void settledContractIsNotMarkedOverdue() {
        Contracts contract = contract("SETTLED");
        contract.setExpirationDate(TODAY.minusDays(1));
        when(contractRepository.findContractIdsPastExpiration(any(), any()))
                .thenReturn(List.of(contract.getId()));
        when(contractRepository.findForSigningById(contract.getId())).thenReturn(Optional.of(contract));

        assertThat(service.markOverdueContracts(TODAY, NOW)).isZero();
        assertThat(contract.getContractStatus()).isEqualTo("SETTLED");
        verify(historyRepository, never()).save(any());
    }

    @Test
    void partiallySignedContractPastSigningDeadlineBecomesSigningExpired() {
        Users ceo = user("ceo@example.com");
        Users partner = user("partner@example.com");
        Contracts contract = contract("PENDING_SIGNATURE");
        contract.setSigningDeadline(TODAY.minusDays(1));
        ContractWorkflowStepInstance ceoStep = step(ceo, ContractWorkflowStepState.COMPLETED);
        ContractWorkflowStepInstance partnerStep = step(partner, ContractWorkflowStepState.PENDING);
        when(contractRepository.findContractIdsPastSigningDeadline(
                ContractLifecycleService.PENDING_SIGNATURE_STATUSES, TODAY
        )).thenReturn(List.of(contract.getId()));
        when(contractRepository.findForSigningById(contract.getId())).thenReturn(Optional.of(contract));
        when(stepRepository.findByContractIdOrderByStepOrderAsc(contract.getId()))
                .thenReturn(List.of(ceoStep, partnerStep));
        when(userRepository.findUsersByRoleExcludingStatus("CEO", UserStatus.INACTIVE))
                .thenReturn(List.of(ceo));

        int count = service.expireUnsignedContracts(TODAY, NOW);

        assertThat(count).isEqualTo(1);
        assertThat(contract.getContractStatus()).isEqualTo("SIGNING_EXPIRED");
        assertThat(ceoStep.getStatus()).isEqualTo(ContractWorkflowStepState.COMPLETED);
        assertThat(partnerStep.getStatus()).isEqualTo(ContractWorkflowStepState.CANCELLED);
        // CEO + bên chưa ký đều được báo
        verify(dispatcher, times(2)).notify(any(), eq(contract),
                eq(NotificationType.CONTRACT_SIGNING_EXPIRED), anyString(), anyString(), anyString(), eq(NOW));
    }

    @Test
    void signingDeadlineTodayIsStillValid() {
        Contracts contract = contract("PENDING_SIGNATURE");
        contract.setSigningDeadline(TODAY);
        when(contractRepository.findContractIdsPastSigningDeadline(any(), any()))
                .thenReturn(List.of(contract.getId()));
        when(contractRepository.findForSigningById(contract.getId())).thenReturn(Optional.of(contract));

        assertThat(service.expireUnsignedContracts(TODAY, NOW)).isZero();
        assertThat(contract.getContractStatus()).isEqualTo("PENDING_SIGNATURE");
    }

    private static Contracts contract(String status) {
        Contracts contract = new Contracts();
        contract.setId(UUID.randomUUID());
        contract.setContractNumber("HD-001");
        contract.setContractTitle("Service contract");
        contract.setContractStatus(status);
        return contract;
    }

    private static Users user(String email) {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        return user;
    }

    private static ContractWorkflowStepInstance step(Users assignee, ContractWorkflowStepState state) {
        ContractWorkflowStepInstance step = new ContractWorkflowStepInstance();
        step.setId(UUID.randomUUID());
        step.setActionType(ContractWorkflowActionType.SIGN);
        step.setStatus(state);
        step.setAssignedUser(assignee);
        return step;
    }
}
