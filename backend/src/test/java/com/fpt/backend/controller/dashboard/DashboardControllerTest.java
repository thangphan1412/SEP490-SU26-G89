//package com.fpt.backend.controller.dashboard;
//
//import com.fpt.backend.dto.response.dashboard.DashboardOverviewDTO;
//import com.fpt.backend.dto.response.dashboard.DashboardStatsDTO;
//import com.fpt.backend.service.impl.dashboard.DashboardServiceImpl;
//import com.fpt.backend.util.BaseResponse;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class DashboardControllerTest {
//
//    @Mock
//    private DashboardServiceImpl dashboardService;
//
//    @InjectMocks
//    private DashboardController dashboardController;
//
//    @Test
//    void getOverview_returnsDashboardOverview() {
//        DashboardOverviewDTO overview = DashboardOverviewDTO.builder()
//                .totalAgreements(12)
//                .activeAgreements(6)
//                .pendingSignatures(3)
//                .expiredAgreements(2)
//                .statusDistribution(List.of())
//                .build();
//        when(dashboardService.getOverviewStatistics()).thenReturn(overview);
//
//        ResponseEntity<BaseResponse<DashboardOverviewDTO>> response = dashboardController.getOverview();
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(200);
//            assertThat(body.getMessage()).isEqualTo("Success");
//            assertThat(body.getData()).isSameAs(overview);
//        });
//        verify(dashboardService).getOverviewStatistics();
//    }
//
//    @Test
//    void getStatisticalReports_returnsDashboardReports() {
//        DashboardStatsDTO reports = DashboardStatsDTO.builder()
//                .totalAgreements(12)
//                .activeAgreements(6)
//                .expiredAgreements(2)
//                .canceledAgreements(1)
//                .build();
//        when(dashboardService.getStatisticalReports()).thenReturn(reports);
//
//        ResponseEntity<BaseResponse<DashboardStatsDTO>> response =
//                dashboardController.getStatisticalReports();
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(200);
//            assertThat(body.getMessage()).isEqualTo("Success");
//            assertThat(body.getData()).isSameAs(reports);
//        });
//        verify(dashboardService).getStatisticalReports();
//    }
//
//    @Test
//    void getPendingSignatures_returnsPendingSignatureDashboard() {
//        DashboardStatsDTO pending = DashboardStatsDTO.builder()
//                .totalPending(4)
//                .overdue(1)
//                .dueIn7Days(2)
//                .avgDaysPending(8.5)
//                .build();
//        when(dashboardService.getPendingSignatureDashboard()).thenReturn(pending);
//
//        ResponseEntity<BaseResponse<DashboardStatsDTO>> response =
//                dashboardController.getPendingSignatures();
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(200);
//            assertThat(body.getMessage()).isEqualTo("Success");
//            assertThat(body.getData()).isSameAs(pending);
//        });
//        verify(dashboardService).getPendingSignatureDashboard();
//    }
//
//    @Test
//    void getOverview_propagatesServiceExceptionForGlobalExceptionHandler() {
//        when(dashboardService.getOverviewStatistics())
//                .thenThrow(new RuntimeException("Dashboard data unavailable"));
//
//        assertThatThrownBy(() -> dashboardController.getOverview())
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("Dashboard data unavailable");
//    }
//}
