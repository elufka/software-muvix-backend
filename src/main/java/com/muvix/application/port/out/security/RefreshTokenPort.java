package com.muvix.application.port.out.security;

import java.time.Instant;

public interface RefreshTokenPort {
    void store(String tokenId, Long userId, Instant expiresAt);

    boolean consume(String tokenId, Long userId, Instant now);

    void revoke(String tokenId);

    void revokeAllForUser(Long userId);
}
