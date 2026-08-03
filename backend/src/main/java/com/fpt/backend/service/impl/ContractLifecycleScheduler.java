package com.fpt.backend.service.impl;

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
public class ContractLifecycleScheduler {
    private static final ZoneId CONTRACT_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ContractLifecycleService contractLifecycleService;

    @EventListener(ApplicationReadyEvent.class)
    public void catchUpAfterStartup() {
        runAutoEnd();
    }

    @Scheduled(
            cron = "${contract.lifecycle.auto-end-cron:0 5 0 * * *}",
            zone = "${contract.lifecycle.time-zone:Asia/Ho_Chi_Minh}"
    )
    public void endExpiredContractsDaily() {
        runAutoEnd();
    }

    private void runAutoEnd() {
        LocalDateTime now = LocalDateTime.now(CONTRACT_TIME_ZONE);

        try {
            int endedCount = contractLifecycleService.endExpiredContracts(
                    now.toLocalDate(),
                    now
            );
            if (endedCount > 0) {
                log.info("Automatically ended {} expired contract(s)", endedCount);
            }
        } catch (RuntimeException exception) {
            log.error("Unable to automatically end expired contracts", exception);
        }
    }
}
