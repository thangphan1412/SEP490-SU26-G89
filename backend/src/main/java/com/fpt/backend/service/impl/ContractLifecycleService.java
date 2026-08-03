package com.fpt.backend.service.impl;

import com.fpt.backend.entity.ContractStatusHistory;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.enums.ContractStatus;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractLifecycleService {
    static final String AUTO_END_ACTION = "AUTO_END";
    static final String SYSTEM_ACTOR = "SYSTEM";
    static final String AUTO_END_COMMENT =
            "Contract automatically ended after expiration date";

    private final ContractRepository contractRepository;
    private final ContractStatusHistoryRepository contractStatusHistoryRepository;

    @Transactional
    public int endExpiredContracts(LocalDate today, LocalDateTime endedAt) {
        Objects.requireNonNull(today, "Today is required");
        Objects.requireNonNull(endedAt, "Ended time is required");

        List<UUID> candidateIds = contractRepository.findExpiredActiveContractIds(
                ContractStatus.ACTIVE.name(),
                today
        );
        int endedCount = 0;

        for (UUID contractId : candidateIds) {
            int updated = contractRepository.markExpiredContractEnded(
                    contractId,
                    ContractStatus.ACTIVE.name(),
                    ContractStatus.ENDED.name(),
                    today,
                    endedAt
            );

            if (updated == 0) {
                continue;
            }

            Contracts contractReference = contractRepository.getReferenceById(contractId);
            contractStatusHistoryRepository.save(
                    ContractStatusHistory.builder()
                            .contract(contractReference)
                            .fromStatus(ContractStatus.ACTIVE.name())
                            .toStatus(ContractStatus.ENDED.name())
                            .action(AUTO_END_ACTION)
                            .actorName(SYSTEM_ACTOR)
                            .actorRole(SYSTEM_ACTOR)
                            .comment(AUTO_END_COMMENT)
                            .signerAgeVerified(null)
                            .changedAt(endedAt)
                            .build()
            );
            endedCount++;
        }

        return endedCount;
    }

    static boolean isExpired(LocalDate expirationDate, LocalDate today) {
        return expirationDate != null
                && today != null
                && expirationDate.isBefore(today);
    }
}
