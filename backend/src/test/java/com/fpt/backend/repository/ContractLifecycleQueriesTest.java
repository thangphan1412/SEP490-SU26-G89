package com.fpt.backend.repository;

import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Notifications;
import com.fpt.backend.entity.Role;
import com.fpt.backend.entity.UserRole;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.notification.NotificationRepository;
import com.fpt.backend.repository.user.UserRepository;
import com.fpt.backend.repository.userRole.UserRoleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:lifecycle;MODE=MSSQLServer",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ContractLifecycleQueriesTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 25);
    private static final List<String> FULLY_SIGNED = List.of("SIGNED", "PENDING_EFFECTIVE", "ACTIVE");
    private static final List<String> PENDING_SIGNATURE =
            List.of("PENDING_SIGNATURE", "PENDING_DIRECTOR_SIGNATURE", "PENDING_PARTNER_SIGNATURE");

    @Autowired private ContractRepository contractRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private EntityManager entityManager;

    @Test
    void pastExpirationQueryFindsOnlyUnsettledContractsExpiredBeforeToday() {
        UUID expiredActive = save("ACTIVE", TODAY.minusDays(30), TODAY.minusDays(1), null);
        save("ACTIVE", TODAY.minusDays(30), TODAY, null);              // hết hạn hôm nay: vẫn còn hiệu lực
        save("SETTLED", TODAY.minusDays(30), TODAY.minusDays(1), null); // đã thanh lý
        save("OVERDUE", TODAY.minusDays(30), TODAY.minusDays(5), null); // đã overdue rồi
        UUID lowerCase = save("active", TODAY.minusDays(30), TODAY.minusDays(2), null);

        assertThat(contractRepository.findContractIdsPastExpiration(FULLY_SIGNED, TODAY))
                .containsExactlyInAnyOrder(expiredActive, lowerCase);
    }

    @Test
    void activateQueryFindsSignedContractsReachingEffectiveDate() {
        UUID effectiveToday = save("PENDING_EFFECTIVE", TODAY, TODAY.plusDays(30), null);
        UUID legacySigned = save("SIGNED", null, TODAY.plusDays(30), null);
        save("PENDING_EFFECTIVE", TODAY.plusDays(1), TODAY.plusDays(30), null); // chưa tới ngày
        save("SIGNED", TODAY.minusDays(40), TODAY.minusDays(1), null);          // đã quá hạn -> overdue, không activate

        assertThat(contractRepository.findContractIdsToActivate(List.of("SIGNED", "PENDING_EFFECTIVE"), TODAY))
                .containsExactlyInAnyOrder(effectiveToday, legacySigned);
    }

    @Test
    void signingDeadlineQueriesUseDeadlineOrFallBackToExpirationDate() {
        UUID pastDeadline = save("PENDING_SIGNATURE", TODAY, TODAY.plusDays(60), TODAY.minusDays(1));
        save("PENDING_SIGNATURE", TODAY, TODAY.plusDays(60), TODAY);             // hạn ký hôm nay: còn ký được
        UUID legacyNoDeadline = save("PENDING_PARTNER_SIGNATURE", TODAY.minusDays(60), TODAY.minusDays(1), null);
        save("ACTIVE", TODAY, TODAY.plusDays(60), TODAY.minusDays(1));           // đã ký đủ, không đụng tới
        UUID dueSoon = save("PENDING_SIGNATURE", TODAY, TODAY.plusDays(60), TODAY.plusDays(3));

        assertThat(contractRepository.findContractIdsPastSigningDeadline(PENDING_SIGNATURE, TODAY))
                .containsExactlyInAnyOrder(pastDeadline, legacyNoDeadline);
        assertThat(contractRepository.findContractsWithSigningDeadlineBetween(PENDING_SIGNATURE, TODAY, TODAY.plusDays(3)))
                .extracting(Contracts::getId)
                .contains(dueSoon)
                .doesNotContain(pastDeadline, legacyNoDeadline);
    }

    @Test
    void ceoLookupMatchesRoleCodeOrNameAndSkipsInactiveUsers() {
        Role ceoRole = new Role();
        ceoRole.setRoleCode("CEO");
        ceoRole.setRoleName("Chief Executive Officer");
        ceoRole.setCreatedAt(LocalDateTime.now());
        entityManager.persist(ceoRole);
        Users activeCeo = saveUser("ceo@example.com", UserStatus.ACTIVE, ceoRole);
        saveUser("old-ceo@example.com", UserStatus.INACTIVE, ceoRole);
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findUsersByRoleExcludingStatus("ceo", UserStatus.INACTIVE))
                .extracting(Users::getId)
                .containsExactly(activeCeo.getId());
    }

    @Test
    void notificationDedupeAndEmailSentFlagWork() {
        Users user = saveUser("ceo2@example.com", UserStatus.ACTIVE, null);
        UUID contractId = save("ACTIVE", TODAY, TODAY.plusDays(5), null);
        Notifications notification = notificationRepository.save(Notifications.builder()
                .title("t").content("c").type("CONTRACT_EXPIRING_SOON").isRead(false)
                .createAt(LocalDateTime.now()).referenceKey("KEY-1").emailSent(false)
                .user(user).contract(contractRepository.getReferenceById(contractId)).build());
        entityManager.flush();

        assertThat(notificationRepository.existsByUserIdAndReferenceKey(user.getId(), "KEY-1")).isTrue();
        assertThat(notificationRepository.existsByUserIdAndReferenceKey(user.getId(), "KEY-2")).isFalse();
        assertThat(notificationRepository.findUnsentContractNotifications(
                "CONTRACT_EXPIRING_SOON", List.of("ACTIVE"), TODAY)).hasSize(1);

        notificationRepository.markEmailSent(notification.getId(), LocalDateTime.now());
        entityManager.flush();
        entityManager.clear();

        assertThat(notificationRepository.findUnsentContractNotifications(
                "CONTRACT_EXPIRING_SOON", List.of("ACTIVE"), TODAY)).isEmpty();
    }

    private UUID save(String status, LocalDate effective, LocalDate expiration, LocalDate signingDeadline) {
        Contracts contract = new Contracts();
        contract.setContractNumber("HD-" + UUID.randomUUID());
        contract.setContractTitle("Contract");
        contract.setContractStatus(status);
        contract.setEffectiveDate(effective);
        contract.setExpirationDate(expiration);
        contract.setSigningDeadline(signingDeadline);
        return contractRepository.saveAndFlush(contract).getId();
    }

    private Users saveUser(String email, UserStatus status, Role role) {
        Users user = new Users();
        user.setEmail(email);
        user.setFirstName("Test");
        user.setStatus(status);
        Users saved = userRepository.saveAndFlush(user);
        if (role != null) {
            userRoleRepository.saveAndFlush(UserRole.builder().user(saved).role(role).build());
        }
        return saved;
    }
}
