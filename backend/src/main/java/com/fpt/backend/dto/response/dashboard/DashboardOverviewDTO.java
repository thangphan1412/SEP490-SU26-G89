package com.fpt.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDTO {
    // 4 Thẻ Metrics ở trên cùng
    private long totalAgreements;
    private long activeAgreements;
    private long pendingSignatures;
    private long expiredAgreements;

    // Dữ liệu cho Biểu đồ Donut (Status)
    private List<ChartData> statusDistribution;

    // Dữ liệu cho Bảng Recent Activity
    private List<ActivityData> recentActivities;

    // Dữ liệu cho Bảng Upcoming Expirations
    private List<ExpirationData> upcomingExpirations;

    // Dữ liệu cho Biểu đồ Line (6 tháng gần nhất)
    private List<MonthlyData> agreementsOverTime;

    @Data
    @Builder
    @AllArgsConstructor
    public static class ChartData {
        private String label;
        private long value;
        private String percent;
        private String color;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class ActivityData {
        private String title;
        private String detail;
        private String status;
        private String tone;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class ExpirationData {
        private String title;
        private String company;
        private String date;
        private String period;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class MonthlyData {
        private String month; // Ví dụ: "Jan '25"
        private long count;   // Số lượng hợp đồng
    }
}