package com.fpt.backend.service.impl.contract;

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
    static final String AUTO_ACTIVATE_ACTION = "AUTO_ACTIVATE";
    static final String SYSTEM_ACTOR = "SYSTEM";
    static final String AUTO_END_COMMENT =
            "Contract automatically ended after expiration date";
    static final String AUTO_ACTIVATE_COMMENT =
            "Contract automatically activated on its effective date";

    private final ContractRepository contractRepository;
    private final ContractStatusHistoryRepository contractStatusHistoryRepository;

    @Transactional
    public int activateEffectiveContracts(
            LocalDate today,
            LocalDateTime activatedAt
    ) {
        Objects.requireNonNull(today, "Today is required");
        Objects.requireNonNull(activatedAt, "Activated time is required");

        List<UUID> candidateIds = contractRepository
                .findEffectivePendingContractIds(
                        ContractStatus.PENDING_EFFECTIVE.name(),
                        today
                );
        int activatedCount = 0;
        for (UUID contractId : candidateIds) {
            int updated = contractRepository.markEffectiveContractActive(
                    contractId,
                    ContractStatus.PENDING_EFFECTIVE.name(),
                    ContractStatus.ACTIVE.name(),
                    today,
                    activatedAt
            );
            if (updated == 0) {
                continue;
            }
            Contracts contractReference = contractRepository.getReferenceById(
                    contractId
            );
            contractStatusHistoryRepository.save(
                    ContractStatusHistory.builder()
                            .contract(contractReference)
                            .fromStatus(ContractStatus.PENDING_EFFECTIVE.name())
                            .toStatus(ContractStatus.ACTIVE.name())
                            .action(AUTO_ACTIVATE_ACTION)
                            .actorName(SYSTEM_ACTOR)
                            .actorRole(SYSTEM_ACTOR)
                            .comment(AUTO_ACTIVATE_COMMENT)
                            .changedAt(activatedAt)
                            .build()
            );
            activatedCount++;
        }
        return activatedCount;
    }

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
