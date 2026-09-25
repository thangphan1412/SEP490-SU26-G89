package com.fpt.backend.service.impl.notification;

import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Notifications;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.ContractStatus;
import com.fpt.backend.enums.NotificationType;
import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.notification.NotificationRepository;
import com.fpt.backend.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Tạo thông báo + gửi mail cho CEO khi hợp đồng sắp đến ngày hết hạn.
 * Mỗi hợp đồng được nhắc tại các mốc cấu hình (mặc định 30, 7, 1 ngày trước hạn);
 * mỗi mốc chỉ gửi một lần cho mỗi CEO nhờ referenceKey.
 */
@Slf4j
@Service
public class ContractExpirationReminderService {
    static final String CEO_ROLE = "CEO";
    static final List<String> REMINDER_STATUSES = List.of(
            ContractStatus.ACTIVE.name(),
            ContractStatus.SIGNED.name(),
            ContractStatus.PENDING_EFFECTIVE.name()
    );

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final List<Integer> reminderDays;

    public ContractExpirationReminderService(
            ContractRepository contractRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            ApplicationEventPublisher eventPublisher,
            @Value("${contract.expiration-reminder.days-before:30,7,1}") List<Integer> reminderDays
    ) {
        this.contractRepository = contractRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.eventPublisher = eventPublisher;
        this.reminderDays = normalizeReminderDays(reminderDays);
    }

    @Transactional
    public int sendExpirationReminders(LocalDate today, LocalDateTime now) {
        Objects.requireNonNull(today, "Today is required");
        Objects.requireNonNull(now, "Current time is required");

        retryUnsentEmails(today);

        if (reminderDays.isEmpty()) {
            return 0;
        }
        List<Contracts> contracts = contractRepository.findContractsExpiringBetween(
                REMINDER_STATUSES,
                today,
                today.plusDays(reminderDays.getLast())
        );
        if (contracts.isEmpty()) {
            return 0;
        }

        List<Users> ceos = userRepository.findUsersByRoleExcludingStatus(
                CEO_ROLE,
                UserStatus.INACTIVE
        );
        if (ceos.isEmpty()) {
            log.warn("{} contract(s) are expiring soon but no active CEO account was found",
                    contracts.size());
            return 0;
        }

        int createdCount = 0;
        for (Contracts contract : contracts) {
            long daysRemaining = ChronoUnit.DAYS.between(today, contract.getExpirationDate());
            Integer milestone = reminderMilestone(daysRemaining, reminderDays);
            if (milestone == null) {
                continue;
            }
            String referenceKey = referenceKey(contract, milestone);

            for (Users ceo : ceos) {
                if (notificationRepository.existsByUserIdAndReferenceKey(ceo.getId(), referenceKey)) {
                    continue;
                }
                Notifications notification = notificationRepository.save(
                        buildNotification(contract, ceo, daysRemaining, referenceKey, now)
                );
                eventPublisher.publishEvent(toEmailEvent(notification, contract, ceo, daysRemaining));
                createdCount++;
            }
        }
        return createdCount;
    }

    private void retryUnsentEmails(LocalDate today) {
        notificationRepository.findUnsentContractNotifications(
                NotificationType.CONTRACT_EXPIRING_SOON.name(),
                REMINDER_STATUSES,
                today
        ).forEach(notification -> eventPublisher.publishEvent(toEmailEvent(
                notification,
                notification.getContract(),
                notification.getUser(),
                ChronoUnit.DAYS.between(today, notification.getContract().getExpirationDate())
        )));
    }

    private Notifications buildNotification(
            Contracts contract,
            Users ceo,
            long daysRemaining,
            String referenceKey,
            LocalDateTime now
    ) {
        return Notifications.builder()
                .title("Contract expiring soon - " + contract.getContractNumber())
                .content("Contract " + contract.getContractNumber()
                        + " (" + contract.getContractTitle() + ") will expire on "
                        + contract.getExpirationDate() + " (" + daysRemainingText(daysRemaining) + ").")
                .type(NotificationType.CONTRACT_EXPIRING_SOON.name())
                .isRead(false)
                .createAt(now)
                .referenceKey(referenceKey)
                .emailSent(false)
                .user(ceo)
                .contract(contract)
                .build();
    }

    private ContractExpirationEmailEvent toEmailEvent(
            Notifications notification,
            Contracts contract,
            Users recipient,
            long daysRemaining
    ) {
        return new ContractExpirationEmailEvent(
                notification.getId(),
                displayName(recipient),
                recipient.getEmail(),
                contract.getContractNumber(),
                contract.getContractTitle(),
                contract.getEffectiveDate(),
                contract.getExpirationDate(),
                daysRemaining
        );
    }

    /**
     * Mốc nhắc nhỏ nhất vẫn >= số ngày còn lại. Nhờ vậy nếu server tắt đúng ngày mốc,
     * lần chạy sau vẫn gửi bù cho mốc đó (key không đổi nên không bị gửi lặp).
     */
    static Integer reminderMilestone(long daysRemaining, List<Integer> sortedReminderDays) {
        if (daysRemaining < 0) {
            return null;
        }
        return sortedReminderDays.stream()
                .filter(day -> day >= daysRemaining)
                .findFirst()
                .orElse(null);
    }

    // Có expirationDate trong key: nếu hợp đồng được gia hạn thì các mốc nhắc sẽ chạy lại.
    static String referenceKey(Contracts contract, int milestone) {
        return NotificationType.CONTRACT_EXPIRING_SOON.name()
                + ":" + contract.getId()
                + ":" + contract.getExpirationDate()
                + ":D" + milestone;
    }

    static String daysRemainingText(long daysRemaining) {
        if (daysRemaining == 0) {
            return "expires today";
        }
        return daysRemaining == 1 ? "1 day remaining" : daysRemaining + " days remaining";
    }

    private static List<Integer> normalizeReminderDays(List<Integer> days) {
        if (days == null) {
            return List.of();
        }
        return days.stream()
                .filter(Objects::nonNull)
                .filter(day -> day >= 0)
                .distinct()
                .sorted()
                .toList();
    }

    private static String displayName(Users user) {
        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return fullName.isEmpty() ? user.getEmail() : fullName;
    }
}
