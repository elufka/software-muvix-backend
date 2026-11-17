package com.muvix.infrastructure.adapters.out.scheduler;

import com.muvix.application.port.out.persistence.UserAccountLifecyclePort;
import com.muvix.application.port.out.security.UserAuthenticationPort;
import com.muvix.application.port.out.security.UserAuthenticationPort.AccountStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;

@Component
public class UserAccountLifecycleScheduler {

    private final UserAccountLifecyclePort lifecyclePort;
    private final UserAuthenticationPort authenticationPort;
    private final Clock clock;
    private final Period inactivityPeriod;

    public UserAccountLifecycleScheduler(UserAccountLifecyclePort lifecyclePort,
                                         UserAuthenticationPort authenticationPort,
                                         Clock clock,
                                         @Value("${muvix.user-lifecycle.inactivity-period:P1Y}") String inactivityPeriod) {
        this.lifecyclePort = lifecyclePort;
        this.authenticationPort = authenticationPort;
        this.clock = clock;
        this.inactivityPeriod = Period.parse(inactivityPeriod);
        if (this.inactivityPeriod.isNegative() || this.inactivityPeriod.isZero()) {
            throw new IllegalArgumentException("Inactivity period must be positive");
        }
    }

    @Scheduled(cron = "${muvix.user-lifecycle.scan-cron:0 0 3 * * *}")
    @Transactional
    public void processLifecycle() {
        lifecyclePort.findAccountsPendingErasureConfirmation().forEach(userId -> {
            if (!lifecyclePort.hasIncompleteTransactions(userId)) {
                authenticationPort.updateAccountStatus(userId, AccountStatus.ERASURE_CONFIRMATION_REQUIRED);
            }
        });

        var cutoff = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC)
                .minus(inactivityPeriod)
                .toInstant(ZoneOffset.UTC);
        lifecyclePort.findActiveAccountIdsInactiveBefore(cutoff).forEach(userId -> {
            if (lifecyclePort.hasIncompleteTransactions(userId)) return;
            if (lifecyclePort.hasAnyTransactions(userId)) {
                authenticationPort.updateAccountStatus(userId, AccountStatus.DEACTIVATED);
            } else if (!lifecyclePort.hasActiveBusinessData(userId)) {
                authenticationPort.updateAccountStatus(userId, AccountStatus.SOFT_DELETED);
            }
        });
    }
}
