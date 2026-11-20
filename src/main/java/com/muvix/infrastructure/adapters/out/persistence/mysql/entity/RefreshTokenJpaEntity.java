package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id")
})
public class RefreshTokenJpaEntity {

    @Id
    @Column(name = "token_id", length = 36, nullable = false)
    private String tokenId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    protected RefreshTokenJpaEntity() {}

    public RefreshTokenJpaEntity(String tokenId, Long userId, Instant expiresAt) {
        this.tokenId = tokenId;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    public String getTokenId() { return tokenId; }
    public Long getUserId() { return userId; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public void revoke() { this.revoked = true; }
}