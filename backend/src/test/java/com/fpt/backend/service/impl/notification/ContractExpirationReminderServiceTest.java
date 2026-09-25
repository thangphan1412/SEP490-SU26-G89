package com.fpt.backend.service.impl.notification;

import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Notifications;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.notification.NotificationRepository;
import com.fpt.backend.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContractExpirationReminderServiceTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 25);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 25, 8, 0);

    private ContractRepository contractRepository;
    private UserRepository userRepository;
    private NotificationRepository notificationRepository;
    private ApplicationEventPublisher eventPublisher;
    private ContractExpirationReminderService service;

    @BeforeEach
    void setUp() {
        contractRepository = mock(ContractRepository.class);
        userRepository = mock(UserRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new ContractExpirationReminderService(
                contractRepository,
                userRepository,
                notificationRepository,
                eventPublisher,
                List.of(1, 30, 7)
        );
        when(notificationRepository.save(any(Notifications.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsNotificationAndPublishesEmailForCeo() {
        Contracts contract = contract(TODAY.plusDays(5));
        Users ceo = ceo();
        when(contractRepository.findContractsExpiringBetween(
                ContractExpirationReminderService.REMINDER_STATUSES, TODAY, TODAY.plusDays(30)
        )).thenReturn(List.of(contract));
        when(userRepository.findUsersByRoleExcludingStatus("CEO", UserStatus.INACTIVE))
                .thenReturn(List.of(ceo));

        int created = service.sendExpirationReminders(TODAY, NOW);

        assertThat(created).isEqualTo(1);
        ArgumentCaptor<Notifications> saved = ArgumentCaptor.forClass(Notifications.class);
        verify(notificationRepository).save(saved.capture());
        assertThat(saved.getValue().getUser()).isSameAs(ceo);
        assertThat(saved.getValue().getContract()).isSameAs(contract);
        assertThat(saved.getValue().getType()).isEqualTo("CONTRACT_EXPIRING_SOON");
        assertThat(saved.getValue().getReferenceKey()).endsWith(":D7");
        assertThat(saved.getValue().getEmailSent()).isFalse();

        ArgumentCaptor<ContractExpirationEmailEvent> event =
                ArgumentCaptor.forClass(ContractExpirationEmailEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue().recipientEmail()).isEqualTo("ceo@example.com");
        assertThat(event.getValue().daysRemaining()).isEqualTo(5);
    }

    @Test
    void skipsWhenMilestoneAlreadyNotified() {
        Users ceo = ceo();
        when(contractRepository.findContractsExpiringBetween(anyList(), any(), any()))
                .thenReturn(List.of(contract(TODAY.plusDays(5))));
        when(userRepository.findUsersByRoleExcludingStatus("CEO", UserStatus.INACTIVE))
                .thenReturn(List.of(ceo));
        when(notificationRepository.existsByUserIdAndReferenceKey(eq(ceo.getId()), anyString()))
                .thenReturn(true);

        assertThat(service.sendExpirationReminders(TODAY, NOW)).isZero();
        verify(notificationRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void reminderMilestoneUsesSmallestMilestoneCoveringRemainingDays() {
        List<Integer> days = List.of(1, 7, 30);
        assertThat(ContractExpirationReminderService.reminderMilestone(30, days)).isEqualTo(30);
        assertThat(ContractExpirationReminderService.reminderMilestone(8, days)).isEqualTo(30);
        assertThat(ContractExpirationReminderService.reminderMilestone(7, days)).isEqualTo(7);
        assertThat(ContractExpirationReminderService.reminderMilestone(0, days)).isEqualTo(1);
        assertThat(ContractExpirationReminderService.reminderMilestone(31, days)).isNull();
        assertThat(ContractExpirationReminderService.reminderMilestone(-1, days)).isNull();
    }

    private static Contracts contract(LocalDate expirationDate) {
        Contracts contract = new Contracts();
        contract.setId(UUID.randomUUID());
        contract.setContractNumber("HD-001");
        contract.setContractTitle("Service contract");
        contract.setContractStatus("ACTIVE");
        contract.setExpirationDate(expirationDate);
        return contract;
    }

    private static Users ceo() {
        Users ceo = new Users();
        ceo.setId(UUID.randomUUID());
        ceo.setEmail("ceo@example.com");
        ceo.setFirstName("Nguyen");
        ceo.setLastName("An");
        return ceo;
    }
}
