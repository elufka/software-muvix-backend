package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.user.User;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.UserJpaEntity;

public final class UserPersistenceMapper {

    private UserPersistenceMapper() {
        // Clase de utilidades: no requiere instancias.
    }

    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone()
        );
    }

    public static UserJpaEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }

        UserJpaEntity entity = new UserJpaEntity();
        updateEntity(domain, entity);
        return entity;
    }

    /** Updates profile fields only, preserving credentials, 2FA, lifecycle and provider state. */
    public static void updateEntity(User domain, UserJpaEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        entity.setName(domain.getName());
        entity.setEmail(domain.getEmail());
        entity.setPhone(domain.getPhone());
    }
}
