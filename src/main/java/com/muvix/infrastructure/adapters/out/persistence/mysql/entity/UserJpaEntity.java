package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    @ElementCollection
    @CollectionTable(name = "user_two_factor_recovery_codes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "code_hash", nullable = false)
    private Set<String> recoveryCodeHashes = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 40)
    private AccountStatusJpa accountStatus = AccountStatusJpa.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 24)
    private RoleJpa role = RoleJpa.USER;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "erasure_requested_at")
    private Instant erasureRequestedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_tier")
    private VerificationTierJpa verificationTier;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public enum VerificationTierJpa { UNVERIFIED, BASIC_2FA, IDENTITY_VERIFIED }
    public enum AccountStatusJpa {
        ACTIVE,
        SOFT_DELETED,
        DEACTIVATED,
        ERASURE_PENDING_TRANSACTIONS,
        ERASURE_CONFIRMATION_REQUIRED,
        ERASED
    }
    public enum RoleJpa { USER, MODERATOR }

    @Column(name = "mobbex_connect_id")
    private String mobbexConnectId;

    @Column(name = "mobbex_linked")
    private boolean mobbexLinked;

    // Getters y Setters mobbex
    public String getMobbexConnectId() { return mobbexConnectId; }
    public void setMobbexConnectId(String mobbexConnectId) { this.mobbexConnectId = mobbexConnectId; }
    public boolean isMobbexLinked() { return mobbexLinked; }
    public void setMobbexLinked(boolean mobbexLinked) { this.mobbexLinked = mobbexLinked; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Set<String> getRecoveryCodeHashes() { return recoveryCodeHashes; }
    public void setRecoveryCodeHashes(Set<String> recoveryCodeHashes) { this.recoveryCodeHashes = recoveryCodeHashes; }
    public AccountStatusJpa getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatusJpa accountStatus) { this.accountStatus = accountStatus; }
    public RoleJpa getRole() { return role; }
    public void setRole(RoleJpa role) { this.role = role; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public Instant getErasureRequestedAt() { return erasureRequestedAt; }
    public void setErasureRequestedAt(Instant erasureRequestedAt) { this.erasureRequestedAt = erasureRequestedAt; }
    // Getters y Setters regular
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
    public String getTwoFactorSecret() { return twoFactorSecret; }
    public void setTwoFactorSecret(String twoFactorSecret) { this.twoFactorSecret = twoFactorSecret; }
    public VerificationTierJpa getVerificationTier() { return verificationTier; }
    public void setVerificationTier(VerificationTierJpa verificationTier) { this.verificationTier = verificationTier; }
}
