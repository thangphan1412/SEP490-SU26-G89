package com.fpt.backend.service.impl.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractExpirationReminderScheduler {
    private static final ZoneId CONTRACT_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ContractExpirationReminderService reminderService;

    @EventListener(ApplicationReadyEvent.class)
    public void catchUpAfterStartup() {
        runReminders();
    }

    @Scheduled(
            cron = "${contract.expiration-reminder.cron:0 0 8 * * *}",
            zone = "${contract.lifecycle.time-zone:Asia/Ho_Chi_Minh}"
    )
    public void remindExpiringContractsDaily() {
        runReminders();
    }

    private void runReminders() {
        LocalDateTime now = LocalDateTime.now(CONTRACT_TIME_ZONE);

        try {
            int createdCount = reminderService.sendExpirationReminders(now.toLocalDate(), now);
            if (createdCount > 0) {
                log.info("Created {} contract expiration reminder(s) for CEO", createdCount);
            }
        } catch (RuntimeException exception) {
            log.error("Unable to send contract expiration reminders", exception);
        }
    }
}
