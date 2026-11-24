package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;

import com.muvix.application.port.out.security.UserAuthenticationPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.UserJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.UserMysqlRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Component
@Transactional
public class UserAuthenticationMySQLAdapter implements UserAuthenticationPort {

    private final UserMysqlRepository repository;

    public UserAuthenticationMySQLAdapter(UserMysqlRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthenticationAccount> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthenticationAccount> findById(Long userId) {
        return repository.findById(userId).map(this::toAccount);
    }

    @Override
    public void storeCredentials(Long userId, String passwordHash, String twoFactorSecret) {
        UserJpaEntity user = findUser(userId);
        user.setPasswordHash(passwordHash);
        user.setTwoFactorSecret(twoFactorSecret);
        user.setTwoFactorEnabled(false);
        user.getRecoveryCodeHashes().clear();
    }

    @Override
    public void enableTwoFactor(Long userId, Set<String> recoveryCodeHashes) {
        UserJpaEntity user = findUser(userId);
        user.setTwoFactorEnabled(true);
        user.setRecoveryCodeHashes(recoveryCodeHashes == null ? new java.util.HashSet<>() : new java.util.HashSet<>(recoveryCodeHashes));
        user.setVerificationTier(UserJpaEntity.VerificationTierJpa.BASIC_2FA);
    }

    @Override
    public void disableTwoFactor(Long userId) {
        UserJpaEntity user = findUser(userId);
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.getRecoveryCodeHashes().clear();
        user.setVerificationTier(UserJpaEntity.VerificationTierJpa.UNVERIFIED);
    }

    @Override
    public boolean consumeRecoveryCode(Long userId, String codeHash) {
        UserJpaEntity user = findUser(userId);
        boolean removed = user.getRecoveryCodeHashes().remove(codeHash);
        if (removed) repository.save(user);
        return removed;
    }

    @Override
    public void recordSuccessfulLogin(Long userId, Instant occurredAt) {
        findUser(userId).setLastLoginAt(occurredAt);
    }

    @Override
    public void updateAccountStatus(Long userId, AccountStatus status) {
        UserJpaEntity user = findUser(userId);
        user.setAccountStatus(UserJpaEntity.AccountStatusJpa.valueOf(status.name()));
        if ((status == AccountStatus.ERASURE_PENDING_TRANSACTIONS
                || status == AccountStatus.ERASURE_CONFIRMATION_REQUIRED)
                && user.getErasureRequestedAt() == null) {
            user.setErasureRequestedAt(Instant.now());
        }
    }

    @Override
    public void eraseSensitiveData(Long userId, String replacementEmail) {
        UserJpaEntity user = findUser(userId);
        user.setEmail(replacementEmail);
        user.setPhone(null);
        user.setPasswordHash(null);
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.getRecoveryCodeHashes().clear();
        user.setVerificationTier(UserJpaEntity.VerificationTierJpa.UNVERIFIED);
        user.setMobbexConnectId(null);
        user.setMobbexLinked(false);
        user.setErasureRequestedAt(null);
        user.setAccountStatus(UserJpaEntity.AccountStatusJpa.ERASED);
    }

    private UserJpaEntity findUser(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new DomainException("Usuario no encontrado con ID: " + userId));
    }

    private AuthenticationAccount toAccount(UserJpaEntity entity) {
        return new AuthenticationAccount(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getTwoFactorSecret(),
                entity.isTwoFactorEnabled(),
                Set.copyOf(entity.getRecoveryCodeHashes()),
                AccountStatus.valueOf(entity.getAccountStatus().name()),
                entity.getRole().name()
        );
    }
}
