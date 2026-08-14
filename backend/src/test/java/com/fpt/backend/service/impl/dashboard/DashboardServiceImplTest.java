//package com.fpt.backend.service.impl.dashboard;
//
//import com.fpt.backend.dto.response.dashboard.DashboardOverviewDTO;
//import com.fpt.backend.dto.response.dashboard.DashboardStatsDTO;
//import com.fpt.backend.entity.Contracts;
//import com.fpt.backend.enums.ContractStatus;
//import com.fpt.backend.repository.contract.ContractRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Pageable;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.within;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class DashboardServiceImplTest {
//
//    @Mock
//    private ContractRepository contractRepository;
//
//    @Spy
//    @InjectMocks
//    private DashboardServiceImpl dashboardService;
//
//    @Test
//    void getOverviewStatistics_buildsMetricsStatusChartAndUpcomingExpirations() {
//        LocalDate today = LocalDate.now();
//        Contracts expiringContract = contract("Supplier agreement", today.plusDays(8));
//        LocalDateTime currentMonth = LocalDateTime.now();
//        List<Object[]> monthlyCounts = List.of(
//                new Object[]{currentMonth.minusMonths(1).getYear(), currentMonth.minusMonths(1).getMonthValue(), 3L},
//                new Object[]{currentMonth.getYear(), currentMonth.getMonthValue(), 7L}
//        );
//        stubOverviewRepository(
//                10,
//                4,
//                2,
//                1,
//                3,
//                List.of(
//                        new Object[]{"ACTIVE", 4L},
//                        new Object[]{"PENDING_SIGNATURE", 3L},
//                        new Object[]{"EXPIRED", 2L},
//                        new Object[]{"DRAFT", 1L}
//                ),
//                List.of(expiringContract),
//                monthlyCounts
//        );
//
//        DashboardOverviewDTO result = dashboardService.getOverviewStatistics();
//
//        assertThat(result.getTotalAgreements()).isEqualTo(10);
//        assertThat(result.getActiveAgreements()).isEqualTo(4);
//        assertThat(result.getPendingSignatures()).isEqualTo(3);
//        assertThat(result.getExpiredAgreements()).isEqualTo(3);
//        assertThat(result.getStatusDistribution())
//                .extracting(DashboardOverviewDTO.ChartData::getPercent)
//                .containsExactly("40.0%", "30.0%", "20.0%", "10.0%");
//        assertThat(result.getStatusDistribution())
//                .extracting(DashboardOverviewDTO.ChartData::getColor)
//                .containsExactly("#2361ed", "#ff8909", "#fa4455", "#9eabc0");
//        assertThat(result.getUpcomingExpirations()).singleElement().satisfies(expiration -> {
//            assertThat(expiration.getTitle()).isEqualTo("Supplier agreement");
//            assertThat(expiration.getCompany()).isEqualTo("Internal");
//            assertThat(expiration.getDate()).isEqualTo(today.plusDays(8)
//                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
//            assertThat(expiration.getPeriod()).isEqualTo("In 8 days");
//        });
//        assertThat(result.getAgreementsOverTime())
//                .extracting(DashboardOverviewDTO.MonthlyData::getCount)
//                .containsExactly(0L, 0L, 0L, 0L, 3L, 7L);
//
//        ArgumentCaptor<LocalDate> startDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
//        ArgumentCaptor<LocalDate> endDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
//        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
//        verify(contractRepository).findUpcomingExpirations(
//                startDateCaptor.capture(), endDateCaptor.capture(), pageableCaptor.capture()
//        );
//        assertThat(endDateCaptor.getValue()).isEqualTo(startDateCaptor.getValue().plusDays(30));
//        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
//        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
//    }
//
//    @Test
//    void getOverviewStatistics_usesZeroPercentagesWhenThereAreNoContracts() {
//        stubOverviewRepository(
//                0,
//                0,
//                0,
//                0,
//                0,
//                List.<Object[]>of(new Object[]{"ACTIVE", 0L}),
//                List.of(),
//                List.of()
//        );
//
//        DashboardOverviewDTO result = dashboardService.getOverviewStatistics();
//
//        assertThat(result.getTotalAgreements()).isZero();
//        assertThat(result.getStatusDistribution()).singleElement().satisfies(status -> {
//            assertThat(status.getPercent()).isEqualTo("0.0%");
//            assertThat(status.getColor()).isEqualTo("#2361ed");
//        });
//        assertThat(result.getUpcomingExpirations()).isEmpty();
//        assertThat(result.getAgreementsOverTime()).hasSize(6).allSatisfy(month ->
//                assertThat(month.getCount()).isZero()
//        );
//    }
//
//    @Test
//    void getOverviewStatistics_mapsNullAndUnknownStatusesToFallbackPresentation() {
//        stubOverviewRepository(
//                3,
//                1,
//                0,
//                0,
//                0,
//                List.of(
//                        new Object[]{null, 1L},
//                        new Object[]{"PENDING_DIRECTOR_SIGNATURE", 2L}
//                ),
//                List.of(),
//                List.of()
//        );
//
//        DashboardOverviewDTO result = dashboardService.getOverviewStatistics();
//
//        assertThat(result.getStatusDistribution()).extracting(DashboardOverviewDTO.ChartData::getLabel)
//                .containsExactly("UNKNOWN", "PENDING_DIRECTOR_SIGNATURE");
//        assertThat(result.getStatusDistribution()).extracting(DashboardOverviewDTO.ChartData::getColor)
//                .containsExactly("#4d5c74", "#4d5c74");
//        assertThat(result.getStatusDistribution()).extracting(DashboardOverviewDTO.ChartData::getPercent)
//                .containsExactly("33.3%", "66.7%");
//    }
//
//    @Test
//    void getStatisticalReports_buildsTypeDistributionsCyclesColorsAndReusesOverview() {
//        DashboardOverviewDTO overview = DashboardOverviewDTO.builder()
//                .activeAgreements(4)
//                .expiredAgreements(2)
//                .statusDistribution(List.of(
//                        new DashboardOverviewDTO.ChartData("ACTIVE", 4, "57.1%", "#2361ed")
//                ))
//                .agreementsOverTime(List.of(new DashboardOverviewDTO.MonthlyData("Aug '26", 7)))
//                .upcomingExpirations(List.of())
//                .build();
//        List<Object[]> typeCounts = List.of(
//                new Object[]{"Employment", 2L},
//                new Object[]{"Supplier", 1L},
//                new Object[]{"NDA", 1L},
//                new Object[]{"Lease", 1L},
//                new Object[]{"Service", 1L},
//                new Object[]{"Consulting", 1L},
//                new Object[]{null, 0L}
//        );
//        when(contractRepository.count()).thenReturn(7L);
//        when(contractRepository.countContractsByType()).thenReturn(typeCounts);
//        when(contractRepository.countByContractStatus(ContractStatus.CANCELLED.name())).thenReturn(3L);
//        doReturn(overview).when(dashboardService).getOverviewStatistics();
//
//        DashboardStatsDTO result = dashboardService.getStatisticalReports();
//
//        assertThat(result.getTotalAgreements()).isEqualTo(7);
//        assertThat(result.getActiveAgreements()).isEqualTo(4);
//        assertThat(result.getExpiredAgreements()).isEqualTo(2);
//        assertThat(result.getCanceledAgreements()).isEqualTo(3);
//        assertThat(result.getTypesDistribution()).extracting(DashboardOverviewDTO.ChartData::getLabel)
//                .containsExactly("Employment", "Supplier", "NDA", "Lease", "Service", "Consulting", "Other");
//        assertThat(result.getTypesDistribution()).extracting(DashboardOverviewDTO.ChartData::getColor)
//                .containsExactly("#2361ed", "#ff8909", "#fa4455", "#2ab784", "#7c68bf", "#4d5c74", "#2361ed");
//        assertThat(result.getTopTypes().getFirst().getPercent())
//                .isCloseTo(2 * 100.0 / 7, within(0.000001));
//        assertThat(result.getStatusDistribution()).isSameAs(overview.getStatusDistribution());
//        assertThat(result.getAgreementsOverTime()).isSameAs(overview.getAgreementsOverTime());
//        verify(dashboardService).getOverviewStatistics();
//    }
//
//    @Test
//    void getPendingSignatureDashboard_returnsEmptyChartsForNoPendingContracts() {
//        when(contractRepository.getPendingSignatureDetails()).thenReturn(List.of());
//
//        DashboardStatsDTO result = dashboardService.getPendingSignatureDashboard();
//
//        assertThat(result.getTotalPending()).isZero();
//        assertThat(result.getOverdue()).isZero();
//        assertThat(result.getDueIn7Days()).isZero();
//        assertThat(result.getAvgDaysPending()).isZero();
//        assertThat(result.getPendingByAge()).hasSize(5).allSatisfy(bucket -> {
//            assertThat(bucket.getValue()).isZero();
//            assertThat(bucket.getPercent()).isEqualTo("0.0%");
//        });
//        assertThat(result.getPendingByProject()).isEmpty();
//    }
//
//    @Test
//    void getPendingSignatureDashboard_groupsAgesDeadlinesAndProjects() {
//        LocalDateTime now = LocalDateTime.now();
//        when(contractRepository.getPendingSignatureDetails()).thenReturn(List.of(
//                new Object[]{"Project Alpha", now.minusDays(1)},
//                new Object[]{"Project Alpha", now.minusDays(5)},
//                new Object[]{"Project Beta", now.minusDays(10)},
//                new Object[]{"Project Beta", now.minusDays(20)},
//                new Object[]{"Project Gamma", now.minusDays(31)}
//        ));
//
//        DashboardStatsDTO result = dashboardService.getPendingSignatureDashboard();
//
//        assertThat(result.getTotalPending()).isEqualTo(5);
//        assertThat(result.getDueIn7Days()).isEqualTo(1);
//        assertThat(result.getOverdue()).isEqualTo(2);
//        assertThat(result.getAvgDaysPending()).isEqualTo(13.4);
//        assertThat(result.getPendingByAge()).extracting(DashboardOverviewDTO.ChartData::getValue)
//                .containsExactly(1L, 1L, 1L, 1L, 1L);
//        assertThat(result.getPendingByAge()).extracting(DashboardOverviewDTO.ChartData::getPercent)
//                .containsExactly("20.0%", "20.0%", "20.0%", "20.0%", "20.0%");
//        assertThat(result.getPendingByProject())
//                .extracting(DashboardStatsDTO.ProjectPendingData::getName,
//                        DashboardStatsDTO.ProjectPendingData::getValue)
//                .containsExactlyInAnyOrder(
//                        org.assertj.core.groups.Tuple.tuple("Project Alpha", 2L),
//                        org.assertj.core.groups.Tuple.tuple("Project Beta", 2L),
//                        org.assertj.core.groups.Tuple.tuple("Project Gamma", 1L)
//                );
//    }
//
//    @Test
//    void getPendingSignatureDashboard_assignsNullProjectToGeneralAndDoesNotAddDaysForNullCreatedAt() {
//        when(contractRepository.getPendingSignatureDetails()).thenReturn(List.<Object[]>of(
//                new Object[]{null, null}
//        ));
//
//        DashboardStatsDTO result = dashboardService.getPendingSignatureDashboard();
//
//        assertThat(result.getTotalPending()).isEqualTo(1);
//        assertThat(result.getAvgDaysPending()).isZero();
//        assertThat(result.getPendingByProject()).singleElement().satisfies(project -> {
//            assertThat(project.getName()).isEqualTo("General");
//            assertThat(project.getValue()).isEqualTo(1);
//        });
//        assertThat(result.getPendingByAge()).extracting(DashboardOverviewDTO.ChartData::getValue)
//                .containsExactly(0L, 0L, 0L, 0L, 0L);
//    }
//
//    private void stubOverviewRepository(
//            long total,
//            long active,
//            long pendingDirector,
//            long pendingPartner,
//            long expired,
//            List<Object[]> statusCounts,
//            List<Contracts> upcomingContracts,
//            List<Object[]> monthlyCounts
//    ) {
//        when(contractRepository.count()).thenReturn(total);
//        when(contractRepository.countByContractStatus("ACTIVE")).thenReturn(active);
//        when(contractRepository.countByContractStatus(ContractStatus.PENDING_DIRECTOR_SIGNATURE.name()))
//                .thenReturn(pendingDirector);
//        when(contractRepository.countByContractStatus(ContractStatus.PENDING_PARTNER_SIGNATURE.name()))
//                .thenReturn(pendingPartner);
//        when(contractRepository.countExpiredContracts(any(LocalDate.class))).thenReturn(expired);
//        when(contractRepository.countContractsByStatus()).thenReturn(statusCounts);
//        when(contractRepository.findUpcomingExpirations(
//                any(LocalDate.class), any(LocalDate.class), any(Pageable.class)
//        )).thenReturn(upcomingContracts);
//        when(contractRepository.countContractsByMonth(any(LocalDateTime.class))).thenReturn(monthlyCounts);
//    }
//
//    private static Contracts contract(String title, LocalDate expirationDate) {
//        Contracts contract = new Contracts();
//        contract.setContractTitle(title);
//        contract.setExpirationDate(expirationDate);
//        return contract;
//    }
//}
