//package com.fpt.backend.service.impl.dashboard;
//
//import com.fpt.backend.dto.response.dashboard.DashboardOverviewDTO;
//import com.fpt.backend.dto.response.dashboard.DashboardStatsDTO;
//import com.fpt.backend.repository.contract.ContractRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Pageable;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.within;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class DashboardServiceImplStatusTest {
//
//    @Mock
//    private ContractRepository contractRepository;
//
//    @InjectMocks
//    private DashboardServiceImpl dashboardService;
//
//    @Test
//    void overviewCountsCurrentAndLegacyPendingSignatureStatuses() {
//        when(contractRepository.count()).thenReturn(7L);
//        when(contractRepository.countContractsByStatus()).thenReturn(List.of(
//                new Object[]{"NEW", 1L},
//                new Object[]{"PENDING_APPROVAL", 1L},
//                new Object[]{"PENDING_SIGNATURE", 2L},
//                new Object[]{"PENDING_DIRECTOR_SIGNATURE", 1L},
//                new Object[]{"ACTIVE", 1L},
//                new Object[]{"ENDED", 1L}
//        ));
//        when(contractRepository.findUpcomingExpirations(
//                any(String.class), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)
//        )).thenReturn(List.of());
//        when(contractRepository.countContractsByMonth(any(LocalDateTime.class)))
//                .thenReturn(List.of());
//
//        DashboardOverviewDTO result = dashboardService.getOverviewStatistics();
//
//        assertThat(result.getTotalAgreements()).isEqualTo(7);
//        assertThat(result.getActiveAgreements()).isEqualTo(1);
//        assertThat(result.getPendingSignatures()).isEqualTo(3);
//        assertThat(result.getExpiredAgreements()).isEqualTo(1);
//        assertThat(result.getStatusDistribution())
//                .extracting(DashboardOverviewDTO.ChartData::getLabel)
//                .containsExactly(
//                        "NEW",
//                        "PENDING_APPROVAL",
//                        "PENDING_SIGNATURE",
//                        "PENDING_DIRECTOR_SIGNATURE",
//                        "ACTIVE",
//                        "ENDED"
//                );
//        assertThat(result.getAgreementsOverTime()).hasSize(6)
//                .allSatisfy(month -> assertThat(month.getCount()).isZero());
//
//        verify(contractRepository).findUpcomingExpirations(
//                eq("ACTIVE"), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)
//        );
//    }
//
//    @Test
//    void pendingSignatureDashboardUsesStatusChangedAtAndKeepsUnknownAgesVisible() {
//        LocalDateTime now = LocalDateTime.now();
//        when(contractRepository.getPendingSignatureDetails(any())).thenReturn(List.of(
//                new Object[]{"Project Alpha", now.minusDays(2)},
//                new Object[]{"Project Beta", now.minusDays(10)},
//                new Object[]{"Project Gamma", now.minusDays(16)},
//                new Object[]{null, null}
//        ));
//
//        DashboardStatsDTO result = dashboardService.getPendingSignatureDashboard();
//
//        assertThat(result.getTotalPending()).isEqualTo(4);
//        assertThat(result.getDueIn7Days()).isEqualTo(1);
//        assertThat(result.getOverdue()).isEqualTo(1);
//        assertThat(result.getAvgDaysPending()).isCloseTo(28.0 / 3, within(0.01));
//        assertThat(result.getPendingByAge())
//                .extracting(DashboardOverviewDTO.ChartData::getLabel,
//                        DashboardOverviewDTO.ChartData::getValue)
//                .containsExactly(
//                        org.assertj.core.groups.Tuple.tuple("0 – 3 Days", 1L),
//                        org.assertj.core.groups.Tuple.tuple("4 – 7 Days", 0L),
//                        org.assertj.core.groups.Tuple.tuple("8 – 14 Days", 1L),
//                        org.assertj.core.groups.Tuple.tuple("15 – 30 Days", 1L),
//                        org.assertj.core.groups.Tuple.tuple("> 30 Days", 0L),
//                        org.assertj.core.groups.Tuple.tuple("Unknown", 1L)
//                );
//        assertThat(result.getPendingByProject())
//                .extracting(DashboardStatsDTO.ProjectPendingData::getName,
//                        DashboardStatsDTO.ProjectPendingData::getValue)
//                .containsExactly(
//                        org.assertj.core.groups.Tuple.tuple("Project Alpha", 1L),
//                        org.assertj.core.groups.Tuple.tuple("Project Beta", 1L),
//                        org.assertj.core.groups.Tuple.tuple("Project Gamma", 1L),
//                        org.assertj.core.groups.Tuple.tuple("Standalone", 1L)
//                );
//
//        ArgumentCaptor<List<String>> statuses = ArgumentCaptor.forClass(List.class);
//        verify(contractRepository).getPendingSignatureDetails(statuses.capture());
//        assertThat(statuses.getValue()).containsExactlyInAnyOrder(
//                "PENDING_SIGNATURE",
//                "PENDING_DIRECTOR_SIGNATURE",
//                "PENDING_PARTNER_SIGNATURE"
//        );
//    }
//}
