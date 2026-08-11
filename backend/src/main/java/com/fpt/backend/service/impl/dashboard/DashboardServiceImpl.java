package com.fpt.backend.service.impl.dashboard;

import com.fpt.backend.dto.response.dashboard.DashboardOverviewDTO;
import com.fpt.backend.dto.response.dashboard.DashboardStatsDTO;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.enums.ContractStatus;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.service.interfaces.dashboard.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements IDashboardService { // <-- THÊM CHỖ NÀY

    @Autowired
    private ContractRepository contractRepository;

    @Override // <-- VÀ THÊM ANNOTATION NÀY VÀO TRƯỚC HÀM
    public DashboardOverviewDTO getOverviewStatistics() {
        // 1. Tính toán 4 thẻ Metrics
        long total = contractRepository.count();
        long active = contractRepository.countByContractStatus("ACTIVE");
        long pending = contractRepository.countByContractStatus(ContractStatus.PENDING_DIRECTOR_SIGNATURE.name())
                + contractRepository.countByContractStatus(ContractStatus.PENDING_PARTNER_SIGNATURE.name());
        long expired = contractRepository.countExpiredContracts(java.time.LocalDate.now());

        // 2. Build dữ liệu cho biểu đồ Donut (Status)
        List<Object[]> statusCounts = contractRepository.countContractsByStatus();
        List<DashboardOverviewDTO.ChartData> statusDistribution = new ArrayList<>();

        for (Object[] row : statusCounts) {
            String status = (String) row[0];
            long count = (long) row[1];
            double percentage = total == 0 ? 0 : ((double) count / total) * 100;

            // Map màu sắc theo chuẩn UI của bạn
            String color = switch (status != null ? status : "") {
                case "ACTIVE" -> "#2361ed"; // Xanh dương
                case "PENDING_SIGNATURE" -> "#ff8909"; // Cam
                case "EXPIRED" -> "#fa4455"; // Đỏ
                case "DRAFT" -> "#9eabc0"; // Xám
                default -> "#4d5c74";
            };

            statusDistribution.add(DashboardOverviewDTO.ChartData.builder()
                    .label(status != null ? status : "UNKNOWN")
                    .value(count)
                    .percent(String.format("%.1f%%", percentage))
                    .color(color)
                    .build());
        }

        // 3. Build dữ liệu Upcoming Expirations (Top 5 hợp đồng sắp hết hạn)
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);
        List<Contracts> upcomingContracts = contractRepository.findUpcomingExpirations(today, thirtyDaysLater, PageRequest.of(0, 5));

        List<DashboardOverviewDTO.ExpirationData> expirations = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        for (Contracts c : upcomingContracts) {
            long daysLeft = ChronoUnit.DAYS.between(today, c.getExpirationDate());
            expirations.add(DashboardOverviewDTO.ExpirationData.builder()
                    .title(c.getContractTitle())
                    .company("Internal") // Tạm để Internal, nếu có Partner entity thì join vào
                    .date(c.getExpirationDate().format(formatter))
                    .period("In " + daysLeft + " days")
                    .build());
        }

        // --- BẮT ĐẦU CODE MỚI: XỬ LÝ BIỂU ĐỒ LINE (6 THÁNG GẦN NHẤT) ---
        // 1. Lấy mốc thời gian là ngày mùng 1 của 5 tháng trước (tổng là 6 tháng tính cả tháng này)
        java.time.LocalDateTime sixMonthsAgo = java.time.LocalDateTime.now()
                .minusMonths(5).withDayOfMonth(1).withHour(0).withMinute(0);

        List<Object[]> monthlyCounts = contractRepository.countContractsByMonth(sixMonthsAgo);

        // 2. Chuyển list object thành Map để dễ dò (Key: "Year-Month", Value: Count)
        java.util.Map<String, Long> countMap = new java.util.HashMap<>();
        for (Object[] row : monthlyCounts) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            long count = ((Number) row[2]).longValue();
            countMap.put(year + "-" + month, count);
        }

        // 3. Chạy vòng lặp đúng 6 tháng gần nhất để đảm bảo tháng nào không có hợp đồng thì gán là 0
        List<DashboardOverviewDTO.MonthlyData> agreementsOverTime = new java.util.ArrayList<>();
        java.time.format.DateTimeFormatter monthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM ''yy");

        for (int i = 5; i >= 0; i--) {
            java.time.LocalDateTime monthDate = java.time.LocalDateTime.now().minusMonths(i);
            String key = monthDate.getYear() + "-" + monthDate.getMonthValue();
            long count = countMap.getOrDefault(key, 0L); // Nếu ko có thì trả về 0
            agreementsOverTime.add(new DashboardOverviewDTO.MonthlyData(monthDate.format(monthFormatter), count));
        }
        // --- KẾT THÚC CODE MỚI ---

        // 4. Trả về DTO tổng
        return DashboardOverviewDTO.builder()
                .totalAgreements(total)
                .activeAgreements(active)
                .pendingSignatures(pending)
                .expiredAgreements(expired)
                .statusDistribution(statusDistribution)
                .upcomingExpirations(expirations)
                // Activity Log bạn có thể query từ bảng ActivityLog tương tự
                .recentActivities(new ArrayList<>())
                .agreementsOverTime(agreementsOverTime)
                .build();
    }

    public DashboardStatsDTO getStatisticalReports() {
        long total = contractRepository.count();

        // Lấy thống kê Loại hợp đồng
        List<Object[]> typeCounts = contractRepository.countContractsByType();
        List<DashboardOverviewDTO.ChartData> typesDistribution = new ArrayList<>();
        List<DashboardStatsDTO.TypeCountData> topTypes = new ArrayList<>();

        String[] colors = {"#2361ed", "#ff8909", "#fa4455", "#2ab784", "#7c68bf", "#4d5c74"};
        int colorIdx = 0;

        for (Object[] row : typeCounts) {
            String typeName = row[0] != null ? (String) row[0] : "Other";
            long count = (long) row[1];
            double percent = total == 0 ? 0 : ((double) count / total) * 100;

            typesDistribution.add(DashboardOverviewDTO.ChartData.builder()
                    .label(typeName).value(count).percent(String.format("%.1f%%", percent))
                    .color(colors[colorIdx % colors.length]).build());

            topTypes.add(DashboardStatsDTO.TypeCountData.builder()
                    .name(typeName).count(count).percent(percent).build());
            colorIdx++;
        }

        // Tái sử dụng logic Overview cũ
        DashboardOverviewDTO overview = getOverviewStatistics();

        return DashboardStatsDTO.builder()
                .totalAgreements(total)
                .activeAgreements(overview.getActiveAgreements())
                .expiredAgreements(overview.getExpiredAgreements())
                .canceledAgreements(contractRepository.countByContractStatus(ContractStatus.CANCELLED.name()))
                .typesDistribution(typesDistribution)
                .topTypes(topTypes)
                .statusDistribution(overview.getStatusDistribution())
                .agreementsOverTime(overview.getAgreementsOverTime())
                .topExpiring(overview.getUpcomingExpirations())
                .build();
    }

    public DashboardStatsDTO getPendingSignatureDashboard() {
        List<Object[]> pendingDetails = contractRepository.getPendingSignatureDetails();

        long totalPending = pendingDetails.size();
        long overdue = 0, dueIn7Days = 0;
        long totalDays = 0;

        int age0_3 = 0, age4_7 = 0, age8_14 = 0, age15_30 = 0, ageOver30 = 0;
        java.util.Map<String, Long> projectMap = new java.util.HashMap<>();

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        for (Object[] row : pendingDetails) {
            String project = row[0] != null ? (String) row[0] : "General";
            java.time.LocalDateTime createdAt = (java.time.LocalDateTime) row[1];

            // Cộng dồn dự án
            projectMap.put(project, projectMap.getOrDefault(project, 0L) + 1);

            // Tính toán số ngày
            if (createdAt != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(createdAt, now);
                totalDays += days;

                if (days > 14) overdue++;
                else if (days > 7) dueIn7Days++;

                if (days <= 3) age0_3++;
                else if (days <= 7) age4_7++;
                else if (days <= 14) age8_14++;
                else if (days <= 30) age15_30++;
                else ageOver30++;
            }
        }

        double avgDays = totalPending == 0 ? 0 : (double) totalDays / totalPending;

        // Build Age Chart
        List<DashboardOverviewDTO.ChartData> pendingByAge = new ArrayList<>();
        pendingByAge.add(new DashboardOverviewDTO.ChartData("0 – 3 Days", age0_3, String.format("%.1f%%", totalPending==0?0:(age0_3*100.0/totalPending)), "#2361ed"));
        pendingByAge.add(new DashboardOverviewDTO.ChartData("4 – 7 Days", age4_7, String.format("%.1f%%", totalPending==0?0:(age4_7*100.0/totalPending)), "#2ab784"));
        pendingByAge.add(new DashboardOverviewDTO.ChartData("8 – 14 Days", age8_14, String.format("%.1f%%", totalPending==0?0:(age8_14*100.0/totalPending)), "#ff9800"));
        pendingByAge.add(new DashboardOverviewDTO.ChartData("15 – 30 Days", age15_30, String.format("%.1f%%", totalPending==0?0:(age15_30*100.0/totalPending)), "#fa4455"));
        pendingByAge.add(new DashboardOverviewDTO.ChartData("> 30 Days", ageOver30, String.format("%.1f%%", totalPending==0?0:(ageOver30*100.0/totalPending)), "#63728e"));

        // Build Project Chart
        List<DashboardStatsDTO.ProjectPendingData> pendingByProject = new ArrayList<>();
        projectMap.forEach((k, v) -> pendingByProject.add(new DashboardStatsDTO.ProjectPendingData(k, v)));

        return DashboardStatsDTO.builder()
                .totalPending(totalPending)
                .overdue(overdue)
                .dueIn7Days(dueIn7Days)
                .avgDaysPending(avgDays)
                .pendingByAge(pendingByAge)
                .pendingByProject(pendingByProject)
                .build();
    }
}