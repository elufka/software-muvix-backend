package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;

import com.muvix.application.port.out.persistence.UserRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.user.User;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.UserJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.UserMysqlRepository; // <-- Importación correcta del repositorio
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserMysqlAdapter implements UserRepositoryPort {

    private final UserMysqlRepository userMysqlRepository;

    public UserMysqlAdapter(UserMysqlRepository userMysqlRepository) {
        this.userMysqlRepository = userMysqlRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity saved = userMysqlRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userMysqlRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userMysqlRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public void updateMobbexConnectDetails(Long userId, String mobbexConnectId, boolean linked) {
        UserJpaEntity entity = userMysqlRepository.findById(userId)
                .orElseThrow(() -> new DomainException("Usuario no encontrado con ID: " + userId));
        entity.setMobbexConnectId(mobbexConnectId);
        entity.setMobbexLinked(linked);
        userMysqlRepository.save(entity);
    }

    private UserJpaEntity toEntity(User domain) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setEmail(domain.getEmail());
        entity.setPhone(domain.getPhone());
        return entity;
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone()
        );
    }
}