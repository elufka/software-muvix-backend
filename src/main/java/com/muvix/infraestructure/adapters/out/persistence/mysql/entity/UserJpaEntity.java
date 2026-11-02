package com.muvix.infraestructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

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

    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_tier")
    private VerificationTierJpa verificationTier;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public enum VerificationTierJpa { UNVERIFIED, BASIC_2FA, IDENTITY_VERIFIED }

    // Getters y Setters
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