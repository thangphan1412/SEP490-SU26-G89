package com.fpt.backend.service.impl.contract;

import com.fpt.backend.enums.ContractStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ContractLifecycleRulesTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 25);

    @Test
    void fullySignedContractStatusDependsOnEffectiveAndExpirationDates() {
        assertThat(ContractLifecycleRules.statusAfterFullySigned(TODAY, TODAY.plusDays(30), TODAY))
                .isEqualTo(ContractStatus.ACTIVE);
        assertThat(ContractLifecycleRules.statusAfterFullySigned(TODAY.plusDays(1), TODAY.plusDays(30), TODAY))
                .isEqualTo(ContractStatus.PENDING_EFFECTIVE);
        assertThat(ContractLifecycleRules.statusAfterFullySigned(TODAY.minusDays(30), TODAY.minusDays(1), TODAY))
                .isEqualTo(ContractStatus.OVERDUE);
        assertThat(ContractLifecycleRules.statusAfterFullySigned(null, null, TODAY))
                .isEqualTo(ContractStatus.ACTIVE);
    }

    @Test
    void expirationAndSigningDeadlineAreInclusive() {
        assertThat(ContractLifecycleRules.isPastExpiration(TODAY, TODAY)).isFalse();
        assertThat(ContractLifecycleRules.isPastExpiration(TODAY.minusDays(1), TODAY)).isTrue();
        assertThat(ContractLifecycleRules.isSigningDeadlinePassed(TODAY, TODAY)).isFalse();
        assertThat(ContractLifecycleRules.isSigningDeadlinePassed(TODAY.minusDays(1), TODAY)).isTrue();
        assertThat(ContractLifecycleRules.isSigningDeadlinePassed(null, TODAY)).isFalse();
    }

    @Test
    void defaultSigningDeadlineIsCappedByExpirationDate() {
        assertThat(ContractLifecycleRules.defaultSigningDeadline(TODAY, null, 14))
                .isEqualTo(TODAY.plusDays(14));
        assertThat(ContractLifecycleRules.defaultSigningDeadline(TODAY, TODAY.plusDays(5), 14))
                .isEqualTo(TODAY.plusDays(5));
        assertThat(ContractLifecycleRules.defaultSigningDeadline(TODAY, TODAY.minusDays(5), 14))
                .isEqualTo(TODAY);
    }

    @Test
    void onlyFullySignedContractsCanBeSettled() {
        assertThat(ContractStatus.ACTIVE.canSettle()).isTrue();
        assertThat(ContractStatus.OVERDUE.canSettle()).isTrue();
        assertThat(ContractStatus.PENDING_EFFECTIVE.canSettle()).isTrue();
        assertThat(ContractStatus.PENDING_SIGNATURE.canSettle()).isFalse();
        assertThat(ContractStatus.SETTLED.isTerminal()).isTrue();
        assertThat(ContractStatus.SIGNING_EXPIRED.isTerminal()).isTrue();
        assertThat(ContractStatus.OVERDUE.isTerminal()).isFalse();
    }
}
