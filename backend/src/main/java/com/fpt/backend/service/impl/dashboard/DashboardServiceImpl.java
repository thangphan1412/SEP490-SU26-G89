package com.fpt.backend.service.impl.dashboard;

import com.fpt.backend.dto.response.dashboard.DashboardOverviewDTO;
import com.fpt.backend.dto.response.dashboard.DashboardStatsDTO;
import com.fpt.backend.enums.ContractStatus;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.service.interfaces.dashboard.IDashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements IDashboardService {

    private static final int EXPIRING_CONTRACT_LIMIT = 5;
    private static final int EXPIRING_WINDOW_DAYS = 30;
    private static final int TREND_MONTH_COUNT = 6;
    private static final String UNKNOWN_STATUS = "UNKNOWN";
    private static final String OTHER_TYPE = "Other";

    private static final List<String> STATUS_ORDER = List.of(
            ContractStatus.NEW.name(),
            ContractStatus.PENDING_APPROVAL.name(),
            ContractStatus.PENDING_INTERNAL_APPROVAL.name(),
            ContractStatus.PENDING_SIGNATURE.name(),
            ContractStatus.PENDING_DIRECTOR_SIGNATURE.name(),
            ContractStatus.PENDING_PARTNER_SIGNATURE.name(),
            ContractStatus.PENDING_EFFECTIVE.name(),
            ContractStatus.SIGNED.name(),
            ContractStatus.ACTIVE.name(),
            ContractStatus.ENDED.name(),
            ContractStatus.CANCELLED.name()
    );

    private static final Set<String> PENDING_SIGNATURE_STATUSES = Set.of(
            ContractStatus.PENDING_SIGNATURE.name(),
            ContractStatus.PENDING_DIRECTOR_SIGNATURE.name(),
            ContractStatus.PENDING_PARTNER_SIGNATURE.name()
    );

    private static final Map<String, String> STATUS_COLORS = Map.ofEntries(
            Map.entry(ContractStatus.NEW.name(), "#9eabc0"),
            Map.entry(ContractStatus.PENDING_APPROVAL.name(), "#f59e0b"),
            Map.entry(ContractStatus.PENDING_INTERNAL_APPROVAL.name(), "#f59e0b"),
            Map.entry(ContractStatus.PENDING_SIGNATURE.name(), "#ff8909"),
            Map.entry(ContractStatus.PENDING_DIRECTOR_SIGNATURE.name(), "#f97316"),
            Map.entry(ContractStatus.PENDING_PARTNER_SIGNATURE.name(), "#fb923c"),
            Map.entry(ContractStatus.PENDING_EFFECTIVE.name(), "#7c68bf"),
            Map.entry(ContractStatus.SIGNED.name(), "#0ea5e9"),
            Map.entry(ContractStatus.ACTIVE.name(), "#2ab784"),
            Map.entry(ContractStatus.ENDED.name(), "#fa4455"),
            Map.entry(ContractStatus.CANCELLED.name(), "#64748b"),
            Map.entry(UNKNOWN_STATUS, "#4d5c74")
    );

    private static final String[] TYPE_COLORS = {
            "#2361ed", "#ff8909", "#fa4455", "#2ab784", "#7c68bf", "#4d5c74"
    };

    private final ContractRepository contractRepository;

    @Override
    public DashboardOverviewDTO getOverviewStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        long total = contractRepository.count();
        Map<String, Long> statusCounts = getStatusCounts();

        return DashboardOverviewDTO.builder()
                .totalAgreements(total)
                .activeAgreements(countForStatus(statusCounts, ContractStatus.ACTIVE.name()))
                .pendingSignatures(countForStatuses(
                        statusCounts, PENDING_SIGNATURE_STATUSES
                ))
                .expiredAgreements(countForStatus(statusCounts, ContractStatus.ENDED.name()))
                .statusDistribution(buildStatusDistribution(statusCounts, total))
                .upcomingExpirations(buildUpcomingExpirations(today))
                .recentActivities(List.of())
                .agreementsOverTime(buildAgreementsOverTime(now))
                .build();
    }

    @Override
    public DashboardStatsDTO getStatisticalReports() {
        DashboardOverviewDTO overview = getOverviewStatistics();
        long total = overview.getTotalAgreements();
        Map<String, Long> typeCounts = getTypeCounts();
        List<Map.Entry<String, Long>> sortedTypeCounts = typeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey(
                                String.CASE_INSENSITIVE_ORDER
                        )))
                .toList();
        List<DashboardOverviewDTO.ChartData> typesDistribution = new ArrayList<>();
        List<DashboardStatsDTO.TypeCountData> topTypes = new ArrayList<>();

        for (int index = 0; index < sortedTypeCounts.size(); index++) {
            Map.Entry<String, Long> type = sortedTypeCounts.get(index);
            double percent = percentOf(type.getValue(), total);
            typesDistribution.add(DashboardOverviewDTO.ChartData.builder()
                    .label(type.getKey())
                    .value(type.getValue())
                    .percent(formatPercent(percent))
                    .color(TYPE_COLORS[index % TYPE_COLORS.length])
                    .build());
            topTypes.add(DashboardStatsDTO.TypeCountData.builder()
                    .name(type.getKey())
                    .count(type.getValue())
                    .percent(percent)
                    .build());
        }

        return DashboardStatsDTO.builder()
                .totalAgreements(total)
                .activeAgreements(overview.getActiveAgreements())
                .expiredAgreements(overview.getExpiredAgreements())
                .canceledAgreements(statusValue(
                        overview.getStatusDistribution(), ContractStatus.CANCELLED.name()
                ))
                .typesDistribution(typesDistribution)
                .topTypes(topTypes)
                .statusDistribution(overview.getStatusDistribution())
                .agreementsOverTime(overview.getAgreementsOverTime())
                .topExpiring(overview.getUpcomingExpirations())
                .build();
    }

    @Override
    public DashboardStatsDTO getPendingSignatureDashboard() {
        List<Object[]> pendingDetails = contractRepository.getPendingSignatureDetails(
                List.copyOf(PENDING_SIGNATURE_STATUSES)
        );

        long totalPending = pendingDetails.size();
        long overdue = 0;
        long waitingEightToFourteenDays = 0;
        long totalDays = 0;
        long pendingWithRecordedStart = 0;

        long age0To3 = 0;
        long age4To7 = 0;
        long age8To14 = 0;
        long age15To30 = 0;
        long ageOver30 = 0;
        long ageUnknown = 0;
        Map<String, Long> projectCounts = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (Object[] row : pendingDetails) {
            String project = row[0] instanceof String projectName
                    && !projectName.isBlank() ? projectName : "Standalone";
            LocalDateTime pendingSince = row[1] instanceof LocalDateTime dateTime
                    ? dateTime : null;
            projectCounts.merge(project, 1L, Long::sum);

            if (pendingSince != null) {
                long days = Math.max(0, ChronoUnit.DAYS.between(pendingSince, now));
                totalDays += days;
                pendingWithRecordedStart++;

                if (days > 14) {
                    overdue++;
                } else if (days > 7) {
                    waitingEightToFourteenDays++;
                }

                if (days <= 3) {
                    age0To3++;
                } else if (days <= 7) {
                    age4To7++;
                } else if (days <= 14) {
                    age8To14++;
                } else if (days <= 30) {
                    age15To30++;
                } else {
                    ageOver30++;
                }
            } else {
                ageUnknown++;
            }
        }

        double avgDays = pendingWithRecordedStart == 0
                ? 0 : (double) totalDays / pendingWithRecordedStart;
        List<DashboardOverviewDTO.ChartData> pendingByAge = List.of(
                chartData("0 – 3 Days", age0To3, totalPending, "#2361ed"),
                chartData("4 – 7 Days", age4To7, totalPending, "#2ab784"),
                chartData("8 – 14 Days", age8To14, totalPending, "#ff9800"),
                chartData("15 – 30 Days", age15To30, totalPending, "#fa4455"),
                chartData("> 30 Days", ageOver30, totalPending, "#63728e"),
                chartData("Unknown", ageUnknown, totalPending, "#94a3b8")
        );
        List<DashboardStatsDTO.ProjectPendingData> pendingByProject = projectCounts.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey(
                                String.CASE_INSENSITIVE_ORDER
                        )))
                .map(entry -> new DashboardStatsDTO.ProjectPendingData(
                        entry.getKey(), entry.getValue()
                ))
                .toList();

        return DashboardStatsDTO.builder()
                .totalPending(totalPending)
                .overdue(overdue)
                .dueIn7Days(waitingEightToFourteenDays)
                .avgDaysPending(avgDays)
                .pendingByAge(pendingByAge)
                .pendingByProject(pendingByProject)
                .build();
    }

    private Map<String, Long> getStatusCounts() {
        Map<String, Long> statusCounts = new HashMap<>();
        for (Object[] row : contractRepository.countContractsByStatus()) {
            String status = normalizeStatus(row[0]);
            long count = numberValue(row[1]);
            statusCounts.merge(status, count, Long::sum);
        }
        return statusCounts;
    }

    private Map<String, Long> getTypeCounts() {
        Map<String, Long> typeCounts = new HashMap<>();
        for (Object[] row : contractRepository.countContractsByType()) {
            String typeName = row[0] instanceof String value && !value.isBlank()
                    ? value : OTHER_TYPE;
            typeCounts.merge(typeName, numberValue(row[1]), Long::sum);
        }
        return typeCounts;
    }

    private List<DashboardOverviewDTO.ChartData> buildStatusDistribution(
            Map<String, Long> statusCounts,
            long total
    ) {
        return statusCounts.entrySet().stream()
                .sorted(Comparator
                        .comparingInt((Map.Entry<String, Long> entry) ->
                                statusOrder(entry.getKey()))
                        .thenComparing(Map.Entry.comparingByKey(
                                String.CASE_INSENSITIVE_ORDER
                        )))
                .map(entry -> chartData(
                        entry.getKey(),
                        entry.getValue(),
                        total,
                        STATUS_COLORS.getOrDefault(
                                entry.getKey(), STATUS_COLORS.get(UNKNOWN_STATUS)
                        )
                ))
                .toList();
    }

    private List<DashboardOverviewDTO.ExpirationData> buildUpcomingExpirations(
            LocalDate today
    ) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(
                "MMM dd, yyyy", Locale.ENGLISH
        );
        return contractRepository.findUpcomingExpirations(
                        ContractStatus.ACTIVE.name(),
                        today,
                        today.plusDays(EXPIRING_WINDOW_DAYS),
                        PageRequest.of(0, EXPIRING_CONTRACT_LIMIT)
                )
                .stream()
                .map(contract -> {
                    long daysLeft = ChronoUnit.DAYS.between(
                            today, contract.getExpirationDate()
                    );
                    String projectName = contract.getProject() == null
                            || contract.getProject().getProjectName() == null
                            || contract.getProject().getProjectName().isBlank()
                            ? "Standalone" : contract.getProject().getProjectName();
                    return DashboardOverviewDTO.ExpirationData.builder()
                            .title(contract.getContractTitle())
                            .company(projectName)
                            .date(contract.getExpirationDate().format(dateFormatter))
                            .period(formatDaysLeft(daysLeft))
                            .build();
                })
                .toList();
    }

    private List<DashboardOverviewDTO.MonthlyData> buildAgreementsOverTime(
            LocalDateTime now
    ) {
        YearMonth currentMonth = YearMonth.from(now);
        YearMonth firstMonth = currentMonth.minusMonths(TREND_MONTH_COUNT - 1);
        Map<YearMonth, Long> countsByMonth = new HashMap<>();
        for (Object[] row : contractRepository.countContractsByMonth(
                firstMonth.atDay(1).atStartOfDay()
        )) {
            YearMonth month = YearMonth.of(
                    ((Number) row[0]).intValue(),
                    ((Number) row[1]).intValue()
            );
            countsByMonth.put(month, numberValue(row[2]));
        }

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern(
                "MMM ''yy", Locale.ENGLISH
        );
        List<DashboardOverviewDTO.MonthlyData> trend = new ArrayList<>();
        for (int offset = 0; offset < TREND_MONTH_COUNT; offset++) {
            YearMonth month = firstMonth.plusMonths(offset);
            trend.add(new DashboardOverviewDTO.MonthlyData(
                    month.atDay(1).format(monthFormatter),
                    countsByMonth.getOrDefault(month, 0L)
            ));
        }
        return trend;
    }

    private DashboardOverviewDTO.ChartData chartData(
            String label,
            long value,
            long total,
            String color
    ) {
        return DashboardOverviewDTO.ChartData.builder()
                .label(label)
                .value(value)
                .percent(formatPercent(percentOf(value, total)))
                .color(color)
                .build();
    }

    private String normalizeStatus(Object rawStatus) {
        if (!(rawStatus instanceof String value) || value.isBlank()) {
            return UNKNOWN_STATUS;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT)
                .replace(' ', '_');
        return switch (normalized) {
            case "DRAFT" -> ContractStatus.NEW.name();
            case "PENDING" -> ContractStatus.PENDING_INTERNAL_APPROVAL.name();
            case "REJECTED", "CANCELED", "CANCEL" -> ContractStatus.CANCELLED.name();
            case "COMPLETED", "EXPIRED", "END" -> ContractStatus.ENDED.name();
            default -> normalized;
        };
    }

    private long countForStatus(Map<String, Long> statusCounts, String status) {
        return statusCounts.getOrDefault(status, 0L);
    }

    private long countForStatuses(
            Map<String, Long> statusCounts,
            Set<String> statuses
    ) {
        return statuses.stream()
                .mapToLong(status -> countForStatus(statusCounts, status))
                .sum();
    }

    private long statusValue(
            List<DashboardOverviewDTO.ChartData> statusDistribution,
            String status
    ) {
        return statusDistribution.stream()
                .filter(item -> status.equals(item.getLabel()))
                .mapToLong(DashboardOverviewDTO.ChartData::getValue)
                .sum();
    }

    private int statusOrder(String status) {
        int index = STATUS_ORDER.indexOf(status);
        return index >= 0 ? index : STATUS_ORDER.size();
    }

    private long numberValue(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private double percentOf(long value, long total) {
        return total == 0 ? 0 : value * 100.0 / total;
    }

    private String formatPercent(double value) {
        return String.format(Locale.ROOT, "%.1f%%", value);
    }

    private String formatDaysLeft(long daysLeft) {
        if (daysLeft == 0) {
            return "Today";
        }
        return daysLeft == 1 ? "In 1 day" : "In " + daysLeft + " days";
    }
}
