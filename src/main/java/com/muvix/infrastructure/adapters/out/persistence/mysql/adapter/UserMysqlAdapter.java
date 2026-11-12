package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;

import com.muvix.application.port.out.persistence.UserRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.user.User;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.UserJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.mapper.UserPersistenceMapper;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.UserMysqlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserMysqlAdapter implements UserRepositoryPort {

    private final UserMysqlRepository userMysqlRepository;


    @Override
    public User save(User user) {
        UserJpaEntity entity = UserPersistenceMapper.toEntity(user);
        UserJpaEntity saved = userMysqlRepository.save(entity);
        return UserPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userMysqlRepository.findById(id).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userMysqlRepository.findByEmail(email).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public void updateMobbexConnectDetails(Long userId, String mobbexConnectId, boolean linked) {
        UserJpaEntity entity = userMysqlRepository.findById(userId)
                .orElseThrow(() -> new DomainException("Usuario no encontrado con ID: " + userId));
        entity.setMobbexConnectId(mobbexConnectId);
        entity.setMobbexLinked(linked);
        userMysqlRepository.save(entity);
    }


}