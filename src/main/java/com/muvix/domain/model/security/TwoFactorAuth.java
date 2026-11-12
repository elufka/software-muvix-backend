package com.muvix.domain.model.security;

import com.muvix.domain.model.exception.DomainException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class TwoFactorAuth {

    private final Long userId;
    private String secretKey;
    private boolean enabled;
    private Instant enabledAt;
    private final Set<String> recoveryCodes = new HashSet<>();

    public TwoFactorAuth(Long userId, String secretKey) {
        if (userId == null) throw new DomainException("User ID is required for 2FA");
        if (secretKey == null || secretKey.isBlank()) throw new DomainException("Secret key is required");
        this.userId = userId;
        this.secretKey = secretKey;
        this.enabled = false;
    }

    public void enable(String verificationCode, Set<String> generatedRecoveryCodes) {
        if (this.enabled) throw new DomainException("2FA is already enabled");
        if (verificationCode == null || verificationCode.length() != 6) {
            throw new DomainException("Invalid 2FA verification code");
        }
        this.enabled = true;
        this.enabledAt = Instant.now();
        if (generatedRecoveryCodes != null) {
            this.recoveryCodes.addAll(generatedRecoveryCodes);
        }
    }

    public void disable() {
        if (!this.enabled) throw new DomainException("2FA is not enabled");
        this.enabled = false;
        this.recoveryCodes.clear();
    }

    public boolean useRecoveryCode(String code) {
        if (!enabled) throw new DomainException("2FA is disabled");
        return recoveryCodes.remove(code);
    }

    public Long getUserId() { return userId; }
    public String getSecretKey() { return secretKey; }
    public boolean isEnabled() { return enabled; }
    public Instant getEnabledAt() { return enabledAt; }
    public Set<String> getRecoveryCodes() { return Set.copyOf(recoveryCodes); }
}
