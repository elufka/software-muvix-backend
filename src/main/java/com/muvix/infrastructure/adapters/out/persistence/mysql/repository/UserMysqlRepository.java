package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.time.Instant;
import java.util.List;

@Repository
public interface UserMysqlRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("select u.id from UserJpaEntity u where u.accountStatus = :status and ((u.lastLoginAt is not null and u.lastLoginAt < :cutoff) or (u.lastLoginAt is null and u.createdAt < :cutoff))")
    List<Long> findActiveIdsInactiveBefore(@Param("status") UserJpaEntity.AccountStatusJpa status,
                                           @Param("cutoff") Instant cutoff);

    List<UserJpaEntity> findByAccountStatus(UserJpaEntity.AccountStatusJpa accountStatus);
}
