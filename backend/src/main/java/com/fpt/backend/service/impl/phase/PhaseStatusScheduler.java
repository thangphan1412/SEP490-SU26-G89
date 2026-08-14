package com.fpt.backend.service.impl.phase;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhaseStatusScheduler {
    private final PhaseStatusService phaseStatusService;

    // Làm mới trạng thái và tiến độ của mọi phase sau khi ứng dụng khởi động.
    @EventListener(ApplicationReadyEvent.class)
    public void refreshAfterStartup() {
        phaseStatusService.refreshAllProjectStatuses();
    }

    // Làm mới trạng thái và tiến độ phase mỗi ngày theo lịch cấu hình.
    @Scheduled(
            cron = "${phase.status.refresh-cron:0 1 0 * * *}",
            zone = "${phase.status.time-zone:Asia/Ho_Chi_Minh}"
    )
    public void refreshEveryDay() {
        phaseStatusService.refreshAllProjectStatuses();
    }
}
