package com.fpt.backend.service.interfaces.dashboard;

import com.fpt.backend.dto.response.dashboard.DashboardOverviewDTO;
import com.fpt.backend.dto.response.dashboard.DashboardStatsDTO;

public interface IDashboardService {
    DashboardOverviewDTO getOverviewStatistics();
    DashboardStatsDTO getStatisticalReports();
    DashboardStatsDTO getPendingSignatureDashboard();

}
