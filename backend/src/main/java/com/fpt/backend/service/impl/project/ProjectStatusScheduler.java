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

    @EventListener(ApplicationReadyEvent.class)
    public void refreshAfterStartup() {
        projectStatusService.refreshPlanningProjects();
    }

    @Scheduled(
            cron = "${project.status.refresh-cron:0 0 0 * * *}",
            zone = "${project.status.time-zone:Asia/Ho_Chi_Minh}"
    )
    public void refreshEveryDay() {
        projectStatusService.refreshPlanningProjects();
    }
}
