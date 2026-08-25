package com.fpt.backend.service.impl.project;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectStatusScheduler {
    private final ProjectStatusService projectStatusService;

    // Kích hoạt các dự án đã đến ngày bắt đầu sau khi ứng dụng khởi động.
    @EventListener(ApplicationReadyEvent.class)
    public void refreshAfterStartup() {
        projectStatusService.refreshPlanningProjects();
    }

    // Kiểm tra và kích hoạt dự án Planning mỗi ngày theo lịch cấu hình.
    @Scheduled(
            cron = "${project.status.refresh-cron:0 0 0 * * *}",
            zone = "${project.status.time-zone:Asia/Ho_Chi_Minh}"
    )
    public void refreshEveryDay() {
        projectStatusService.refreshPlanningProjects();
    }
}
