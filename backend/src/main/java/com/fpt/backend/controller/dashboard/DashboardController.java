package com.fpt.backend.controller.dashboard;

import com.fpt.backend.dto.response.dashboard.DashboardOverviewDTO;
import com.fpt.backend.dto.response.dashboard.DashboardStatsDTO;
import com.fpt.backend.service.impl.dashboard.DashboardServiceImpl;
import com.fpt.backend.util.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @Autowired
    private DashboardServiceImpl dashboardService;

    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment')")
    @GetMapping("/overview")
    public ResponseEntity<BaseResponse<DashboardOverviewDTO>> getOverview() {
        DashboardOverviewDTO data = dashboardService.getOverviewStatistics();
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "Success", data));
    }

    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment')")
    @GetMapping("/statistical-reports")
    public ResponseEntity<BaseResponse<DashboardStatsDTO>> getStatisticalReports() {
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "Success", dashboardService.getStatisticalReports()));
    }

    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment')")
    @GetMapping("/pending-signatures")
    public ResponseEntity<BaseResponse<DashboardStatsDTO>> getPendingSignatures() {
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "Success", dashboardService.getPendingSignatureDashboard()));
    }
}