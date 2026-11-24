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

        User.Role domainRole = (entity.getRole() != null)
                ? User.Role.valueOf(entity.getRole().name())
                : User.Role.USER;

        return new User(
                entity.getId(),
                entity.getPhoto(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getCountry(),
                entity.getProvince(),
                entity.getCity(),
                entity.getAddress(),
                domainRole
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

        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setEmail(domain.getEmail());
        entity.setPhone(domain.getPhone());
        entity.setCountry(domain.getCountry());
        entity.setProvince(domain.getProvince());
        entity.setCity(domain.getCity());
        entity.setAddress(domain.getAddress());
    }
}
