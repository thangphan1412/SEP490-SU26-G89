package com.fpt.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardStatsDTO {
    // Dành cho màn Statistical Reports
    private long totalAgreements;
    private long activeAgreements;
    // Lifecycle-ended agreements; field name is retained for dashboard API compatibility.
    private long expiredAgreements;
    private long canceledAgreements;
    private List<DashboardOverviewDTO.ChartData> typesDistribution;
    private List<DashboardOverviewDTO.ChartData> statusDistribution;
    private List<DashboardOverviewDTO.MonthlyData> agreementsOverTime;
    private List<DashboardOverviewDTO.ExpirationData> topExpiring;
    private List<TypeCountData> topTypes; // Thay thế cho Value By Type

    // Dành cho màn Pending Signatures
    private long totalPending;
    // Contracts that have waited from 8 through 14 full days.
    private long dueIn7Days;
    // Contracts that have waited more than 14 full days.
    private long overdue;
    private double avgDaysPending;
    private List<DashboardOverviewDTO.ChartData> pendingByAge;
    private List<ProjectPendingData> pendingByProject;

    @Data
    @Builder
    @AllArgsConstructor
    public static class TypeCountData {
        private String name;
        private long count;
        private double percent;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class ProjectPendingData {
        private String name;
        private long value;
    }
}
