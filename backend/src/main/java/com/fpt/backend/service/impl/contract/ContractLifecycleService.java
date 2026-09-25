package com.fpt.backend.service.impl.contract;

import com.fpt.backend.entity.ContractStatusHistory;
import com.fpt.backend.entity.ContractWorkflowStepInstance;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.ContractStatus;
import com.fpt.backend.enums.ContractWorkflowStepState;
import com.fpt.backend.enums.NotificationType;
import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractStatusHistoryRepository;
import com.fpt.backend.repository.contract.ContractWorkflowStepInstanceRepository;
import com.fpt.backend.repository.user.UserRepository;
import com.fpt.backend.service.impl.notification.ContractNotificationDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Các chuyển trạng thái tự động hằng ngày:
 * 1. SIGNED / PENDING_EFFECTIVE  -> ACTIVE           khi tới effective date
 * 2. ACTIVE / PENDING_EFFECTIVE  -> OVERDUE          khi quá expiration date mà chưa thanh lý
 * 3. PENDING_*_SIGNATURE         -> SIGNING_EXPIRED  khi quá hạn ký mà chưa đủ chữ ký
 * 4. Nhắc bên chưa ký trước hạn ký (mặc định 3 và 1 ngày)
 */
@Service
@RequiredArgsConstructor
public class ContractLifecycleService {
    static final String SYSTEM_ACTOR = "SYSTEM";
    static final String AUTO_ACTIVATE_ACTION = "AUTO_ACTIVATE";
    static final String AUTO_OVERDUE_ACTION = "AUTO_OVERDUE";
    static final String AUTO_SIGNING_EXPIRED_ACTION = "AUTO_SIGNING_EXPIRED";
    static final List<Integer> SIGNING_REMINDER_DAYS = List.of(1, 3);

    static final List<String> ACTIVATABLE_STATUSES = List.of(
            ContractStatus.SIGNED.name(),
            ContractStatus.PENDING_EFFECTIVE.name()
    );
    static final List<String> OVERDUE_CANDIDATE_STATUSES = List.of(
            ContractStatus.SIGNED.name(),
            ContractStatus.PENDING_EFFECTIVE.name(),
            ContractStatus.ACTIVE.name()
    );
    static final List<String> PENDING_SIGNATURE_STATUSES = List.of(
            ContractStatus.PENDING_SIGNATURE.name(),
            ContractStatus.PENDING_DIRECTOR_SIGNATURE.name(),
            ContractStatus.PENDING_PARTNER_SIGNATURE.name()
    );

    private final ContractRepository contractRepository;
    private final ContractStatusHistoryRepository contractStatusHistoryRepository;
    private final ContractWorkflowStepInstanceRepository workflowStepRepository;
    private final UserRepository userRepository;
    private final ContractNotificationDispatcher notificationDispatcher;

    @Transactional
    public int activateEffectiveContracts(LocalDate today, LocalDateTime now) {
        Objects.requireNonNull(today, "Today is required");
        int count = 0;
        for (UUID contractId : contractRepository.findContractIdsToActivate(ACTIVATABLE_STATUSES, today)) {
            Contracts contract = contractRepository.findForSigningById(contractId).orElse(null);
            if (contract == null || !ACTIVATABLE_STATUSES.contains(statusOf(contract).name())) {
                continue;
            }
            changeStatus(contract, ContractStatus.ACTIVE, AUTO_ACTIVATE_ACTION,
                    "Contract became effective on " + today, now);
            count++;
        }
        return count;
    }

    @Transactional
    public int markOverdueContracts(LocalDate today, LocalDateTime now) {
        Objects.requireNonNull(today, "Today is required");
        List<UUID> candidateIds = contractRepository.findContractIdsPastExpiration(
                OVERDUE_CANDIDATE_STATUSES, today
        );
        if (candidateIds.isEmpty()) {
            return 0;
        }
        List<Users> ceos = activeCeos();
        int count = 0;
        for (UUID contractId : candidateIds) {
            Contracts contract = contractRepository.findForSigningById(contractId).orElse(null);
            if (contract == null
                    || !OVERDUE_CANDIDATE_STATUSES.contains(statusOf(contract).name())
                    || !ContractLifecycleRules.isPastExpiration(contract.getExpirationDate(), today)) {
                continue;
            }
            contract.setOverdueAt(now);
            changeStatus(contract, ContractStatus.OVERDUE, AUTO_OVERDUE_ACTION,
                    "Expiration date " + contract.getExpirationDate()
                            + " has passed but the contract has not been settled", now);

            String content = "Contract " + contract.getContractNumber()
                    + " (" + contract.getContractTitle() + ") expired on "
                    + contract.getExpirationDate() + " but has not been settled.\n"
                    + "Its status is now OVER DUE.\n\n"
                    + "Please open the contract and use \"Settle contract\" to record the settlement.";
            for (Users recipient : recipients(ceos, contract.getContractCreatedByUser())) {
                notificationDispatcher.notify(
                        recipient,
                        contract,
                        NotificationType.CONTRACT_OVERDUE,
                        NotificationType.CONTRACT_OVERDUE.name() + ":" + contract.getId()
                                + ":" + contract.getExpirationDate(),
                        "Contract overdue for settlement - " + contract.getContractNumber(),
                        content,
                        now
                );
            }
            count++;
        }
        return count;
    }

    @Transactional
    public int expireUnsignedContracts(LocalDate today, LocalDateTime now) {
        Objects.requireNonNull(today, "Today is required");
        List<UUID> candidateIds = contractRepository.findContractIdsPastSigningDeadline(
                PENDING_SIGNATURE_STATUSES, today
        );
        if (candidateIds.isEmpty()) {
            return 0;
        }
        List<Users> ceos = activeCeos();
        int count = 0;
        for (UUID contractId : candidateIds) {
            Contracts contract = contractRepository.findForSigningById(contractId).orElse(null);
            if (contract == null || !statusOf(contract).isPendingSignature()) {
                continue;
            }
            LocalDate deadline = contract.getSigningDeadline() != null
                    ? contract.getSigningDeadline()
                    : contract.getExpirationDate();
            if (!ContractLifecycleRules.isSigningDeadlinePassed(deadline, today)) {
                continue;
            }

            List<ContractWorkflowStepInstance> steps =
                    workflowStepRepository.findByContractIdOrderByStepOrderAsc(contract.getId());
            List<String> signedBy = steps.stream()
                    .filter(step -> step.getActionType().requiresSignature())
                    .filter(step -> step.getStatus() == ContractWorkflowStepState.COMPLETED)
                    .map(ContractLifecycleService::stepAssigneeName)
                    .toList();
            List<Users> pendingSigners = steps.stream()
                    .filter(step -> step.getActionType().requiresSignature())
                    .filter(step -> step.getStatus() == ContractWorkflowStepState.PENDING
                            || step.getStatus() == ContractWorkflowStepState.WAITING)
                    .map(ContractWorkflowStepInstance::getAssignedUser)
                    .filter(Objects::nonNull)
                    .toList();

            String reason = "Signing deadline " + deadline + " passed before all parties signed";
            steps.stream()
                    .filter(step -> step.getStatus() == ContractWorkflowStepState.PENDING
                            || step.getStatus() == ContractWorkflowStepState.WAITING)
                    .forEach(step -> {
                        step.setStatus(ContractWorkflowStepState.CANCELLED);
                        step.setComment(reason);
                    });
            workflowStepRepository.saveAll(steps);
            contract.setContractEndedAt(now);
            changeStatus(contract, ContractStatus.SIGNING_EXPIRED, AUTO_SIGNING_EXPIRED_ACTION, reason, now);

            String content = "Contract " + contract.getContractNumber()
                    + " (" + contract.getContractTitle() + ") was not signed by all parties before the signing deadline "
                    + deadline + ".\n\n"
                    + "Signed by: " + (signedBy.isEmpty() ? "nobody" : String.join(", ", signedBy)) + "\n"
                    + "Not signed by: " + (pendingSigners.isEmpty() ? "-" : String.join(", ",
                    pendingSigners.stream().map(ContractNotificationDispatcher::displayName).toList())) + "\n\n"
                    + "The contract is NOT in effect and its status is now SIGNING EXPIRED. "
                    + "Existing signatures are kept for audit only. "
                    + "Create a replacement contract if the parties still want to proceed.";
            Map<UUID, Users> all = new LinkedHashMap<>();
            recipients(ceos, contract.getContractCreatedByUser()).forEach(user -> all.put(user.getId(), user));
            pendingSigners.forEach(user -> all.putIfAbsent(user.getId(), user));
            for (Users recipient : all.values()) {
                notificationDispatcher.notify(
                        recipient,
                        contract,
                        NotificationType.CONTRACT_SIGNING_EXPIRED,
                        NotificationType.CONTRACT_SIGNING_EXPIRED.name() + ":" + contract.getId(),
                        "Contract signing deadline passed - " + contract.getContractNumber(),
                        content,
                        now
                );
            }
            count++;
        }
        return count;
    }

    @Transactional
    public int remindPendingSigners(LocalDate today, LocalDateTime now) {
        Objects.requireNonNull(today, "Today is required");
        int maxDays = SIGNING_REMINDER_DAYS.getLast();
        int count = 0;
        for (Contracts contract : contractRepository.findContractsWithSigningDeadlineBetween(
                PENDING_SIGNATURE_STATUSES, today, today.plusDays(maxDays))) {
            long daysLeft = ChronoUnit.DAYS.between(today, contract.getSigningDeadline());
            Integer milestone = SIGNING_REMINDER_DAYS.stream()
                    .filter(day -> day >= daysLeft)
                    .findFirst()
                    .orElse(null);
            if (milestone == null) {
                continue;
            }
            ContractWorkflowStepInstance pendingStep = workflowStepRepository
                    .findFirstByContractIdAndStatusOrderByStepOrderAsc(
                            contract.getId(), ContractWorkflowStepState.PENDING
                    )
                    .filter(step -> step.getActionType().requiresSignature())
                    .orElse(null);
            if (pendingStep == null || pendingStep.getAssignedUser() == null) {
                continue;
            }
            String content = "Contract " + contract.getContractNumber()
                    + " (" + contract.getContractTitle() + ") is waiting for your signature.\n"
                    + "Signing deadline: " + contract.getSigningDeadline()
                    + (daysLeft == 0 ? " (today)" : " (" + daysLeft + " day(s) left)") + ".\n\n"
                    + "If the contract is not signed by all parties before the deadline, "
                    + "it will automatically become SIGNING EXPIRED.";
            boolean created = notificationDispatcher.notify(
                    pendingStep.getAssignedUser(),
                    contract,
                    NotificationType.CONTRACT_SIGNING_DEADLINE_SOON,
                    NotificationType.CONTRACT_SIGNING_DEADLINE_SOON.name() + ":" + contract.getId()
                            + ":" + contract.getSigningDeadline() + ":D" + milestone,
                    "Contract signature needed - " + contract.getContractNumber(),
                    content,
                    now
            );
            if (created) {
                count++;
            }
        }
        return count;
    }

    private void changeStatus(
            Contracts contract,
            ContractStatus toStatus,
            String action,
            String comment,
            LocalDateTime now
    ) {
        ContractStatus fromStatus = statusOf(contract);
        contract.setContractStatus(toStatus.name());
        contract.setContractStatusUpdatedAt(now);
        contractRepository.save(contract);
        contractStatusHistoryRepository.save(
                ContractStatusHistory.builder()
                        .contract(contract)
                        .fromStatus(fromStatus.name())
                        .toStatus(toStatus.name())
                        .action(action)
                        .actorName(SYSTEM_ACTOR)
                        .actorRole(SYSTEM_ACTOR)
                        .comment(comment)
                        .signerAgeVerified(null)
                        .changedAt(now)
                        .build()
        );
    }

    private List<Users> activeCeos() {
        return userRepository.findUsersByRoleExcludingStatus("CEO", UserStatus.INACTIVE);
    }

    private static List<Users> recipients(List<Users> ceos, Users creator) {
        Map<UUID, Users> unique = new LinkedHashMap<>();
        ceos.forEach(ceo -> unique.put(ceo.getId(), ceo));
        if (creator != null) {
            unique.putIfAbsent(creator.getId(), creator);
        }
        return List.copyOf(unique.values());
    }

    private static ContractStatus statusOf(Contracts contract) {
        return ContractStatus.fromValue(contract.getContractStatus());
    }

    private static String stepAssigneeName(ContractWorkflowStepInstance step) {
        return step.getAssignedUser() == null
                ? step.getStepName()
                : ContractNotificationDispatcher.displayName(step.getAssignedUser());
    }
}
