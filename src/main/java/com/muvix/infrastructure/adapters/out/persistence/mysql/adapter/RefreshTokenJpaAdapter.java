package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;

import com.muvix.application.port.out.security.RefreshTokenPort;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.RefreshTokenJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.RefreshTokenJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RefreshTokenJpaAdapter  implements RefreshTokenPort {

    private final RefreshTokenJpaRepository repository;

    public RefreshTokenJpaAdapter(RefreshTokenJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void store(String tokenId, Long userId, Instant expiresAt) {
        repository.save(new RefreshTokenJpaEntity(tokenId, userId, expiresAt));
    }

    @Override
    @Transactional
    public boolean consume(String tokenId, Long userId, Instant now) {
        return repository.findById(tokenId)
                .map(token -> {
                    if (token.isRevoked() || !token.getUserId().equals(userId) || !token.getExpiresAt().isAfter(now)) {
                        return false;
                    }
                    token.revoke();
                    repository.save(token);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public void revoke(String tokenId) {
        repository.revokeById(tokenId);
    }

    @Override
    @Transactional
    public void revokeAllForUser(Long userId) {
        repository.revokeAllActiveByUserId(userId);
    }

    @Scheduled(cron = "0 0 3 * * *") // Limpia tokens vencidos todos los días a las 3:00 AM
    @Transactional
    public void cleanupExpiredTokens() {
        repository.deleteExpiredTokens(Instant.now());
    }
}
