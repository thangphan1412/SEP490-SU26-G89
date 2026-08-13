//package com.fpt.backend.service.impl.contract;
//
//import com.fpt.backend.entity.ContractStatusHistory;
//import com.fpt.backend.entity.Contracts;
//import com.fpt.backend.repository.contract.ContractRepository;
//import com.fpt.backend.repository.contract.ContractStatusHistoryRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class ContractLifecycleServiceTest {
//    @Mock
//    private ContractRepository contractRepository;
//
//    @Mock
//    private ContractStatusHistoryRepository contractStatusHistoryRepository;
//
//    @InjectMocks
//    private ContractLifecycleService lifecycleService;
//
//    @Test
//    void endsExpiredActiveContractAndRecordsSystemHistory() {
//        UUID contractId = UUID.randomUUID();
//        LocalDate today = LocalDate.of(2026, 8, 3);
//        LocalDateTime endedAt = LocalDateTime.of(2026, 8, 3, 0, 5);
//        Contracts contractReference = new Contracts();
//
//        when(contractRepository.findExpiredActiveContractIds("ACTIVE", today))
//                .thenReturn(List.of(contractId));
//        when(contractRepository.markExpiredContractEnded(
//                contractId,
//                "ACTIVE",
//                "ENDED",
//                today,
//                endedAt
//        )).thenReturn(1);
//        when(contractRepository.getReferenceById(contractId))
//                .thenReturn(contractReference);
//
//        int endedCount = lifecycleService.endExpiredContracts(today, endedAt);
//
//        assertThat(endedCount).isEqualTo(1);
//        ArgumentCaptor<ContractStatusHistory> historyCaptor =
//                ArgumentCaptor.forClass(ContractStatusHistory.class);
//        verify(contractStatusHistoryRepository).save(historyCaptor.capture());
//
//        ContractStatusHistory history = historyCaptor.getValue();
//        assertThat(history.getContract()).isSameAs(contractReference);
//        assertThat(history.getFromStatus()).isEqualTo("ACTIVE");
//        assertThat(history.getToStatus()).isEqualTo("ENDED");
//        assertThat(history.getAction()).isEqualTo("AUTO_END");
//        assertThat(history.getActorName()).isEqualTo("SYSTEM");
//        assertThat(history.getActorRole()).isEqualTo("SYSTEM");
//        assertThat(history.getChangedAt()).isEqualTo(endedAt);
//    }
//
//    @Test
//    void doesNotCreateDuplicateHistoryWhenAnotherRunAlreadyEndedContract() {
//        UUID contractId = UUID.randomUUID();
//        LocalDate today = LocalDate.of(2026, 8, 3);
//        LocalDateTime endedAt = LocalDateTime.of(2026, 8, 3, 0, 5);
//
//        when(contractRepository.findExpiredActiveContractIds("ACTIVE", today))
//                .thenReturn(List.of(contractId));
//        when(contractRepository.markExpiredContractEnded(
//                contractId,
//                "ACTIVE",
//                "ENDED",
//                today,
//                endedAt
//        )).thenReturn(0);
//
//        int endedCount = lifecycleService.endExpiredContracts(today, endedAt);
//
//        assertThat(endedCount).isZero();
//        verify(contractRepository, never()).getReferenceById(contractId);
//        verify(contractStatusHistoryRepository, never()).save(
//                org.mockito.ArgumentMatchers.any()
//        );
//    }
//
//    @Test
//    void expirationDateIsInclusive() {
//        LocalDate expirationDate = LocalDate.of(2026, 8, 2);
//
//        assertThat(ContractLifecycleService.isExpired(
//                expirationDate,
//                LocalDate.of(2026, 8, 2)
//        )).isFalse();
//        assertThat(ContractLifecycleService.isExpired(
//                expirationDate,
//                LocalDate.of(2026, 8, 3)
//        )).isTrue();
//    }
//}
