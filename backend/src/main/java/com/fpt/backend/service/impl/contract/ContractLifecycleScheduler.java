package com.fpt.backend.service.impl.contract;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.BiFunction;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractLifecycleScheduler {
    private final ContractLifecycleService contractLifecycleService;

    @EventListener(ApplicationReadyEvent.class)
    public void catchUpAfterStartup() {
        runDailyLifecycle();
    }

    @Scheduled(
            cron = "${contract.lifecycle.auto-end-cron:0 5 0 * * *}",
            zone = "${contract.lifecycle.time-zone:Asia/Ho_Chi_Minh}"
    )
    public void runDailyLifecycleJob() {
        runDailyLifecycle();
    }

    private void runDailyLifecycle() {
        LocalDateTime now = LocalDateTime.now(ContractLifecycleRules.CONTRACT_TIME_ZONE);
        // Mỗi bước chạy transaction riêng: bước này lỗi không chặn bước khác
        run("activated", now, contractLifecycleService::activateEffectiveContracts);
        run("marked OVERDUE", now, contractLifecycleService::markOverdueContracts);
        run("marked SIGNING_EXPIRED", now, contractLifecycleService::expireUnsignedContracts);
        run("signing reminder(s) sent for", now, contractLifecycleService::remindPendingSigners);
    }

    private void run(
            String label,
            LocalDateTime now,
            BiFunction<java.time.LocalDate, LocalDateTime, Integer> job
    ) {
        try {
            int count = job.apply(now.toLocalDate(), now);
            if (count > 0) {
                log.info("Contract lifecycle: {} {} contract(s)", label, count);
            }
        } catch (RuntimeException exception) {
            log.error("Contract lifecycle job failed: {}", label, exception);
        }
    }
}
