package com.muvix.application.port.out.security;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public interface UserAuthenticationPort {

    enum AccountStatus {
        ACTIVE,
        SOFT_DELETED,
        DEACTIVATED,
        ERASURE_PENDING_TRANSACTIONS,
        ERASURE_CONFIRMATION_REQUIRED,
        ERASED
    }

    record AuthenticationAccount(
            Long userId,
            String name,
            String email,
            String passwordHash,
            String twoFactorSecret,
            boolean twoFactorEnabled,
            Set<String> recoveryCodeHashes,
            AccountStatus status,
            String role
    ) {}

    Optional<AuthenticationAccount> findByEmail(String email);

    Optional<AuthenticationAccount> findById(Long userId);

    void storeCredentials(Long userId, String passwordHash, String twoFactorSecret);

    void enableTwoFactor(Long userId, Set<String> recoveryCodeHashes);

    void disableTwoFactor(Long userId);

    boolean consumeRecoveryCode(Long userId, String codeHash);

    void recordSuccessfulLogin(Long userId, Instant occurredAt);

    void updateAccountStatus(Long userId, AccountStatus status);

    void eraseSensitiveData(Long userId, String replacementEmail);
}
