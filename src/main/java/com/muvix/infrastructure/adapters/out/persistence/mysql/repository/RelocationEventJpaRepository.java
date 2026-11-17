package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.RelocationEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelocationEventJpaRepository extends JpaRepository<RelocationEventJpaEntity, Long> {

    Optional<RelocationEventJpaEntity> findByShareToken(String shareToken);

    List<RelocationEventJpaEntity> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query("select itemId from RelocationEventJpaEntity e join e.itemIds itemId where e.id = :eventId")
    List<Long> findItemIdsByEventId(@org.springframework.data.repository.query.Param("eventId") Long eventId);
}
