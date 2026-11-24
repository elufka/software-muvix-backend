package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.ChatMessageTemplateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageTemplateJpaRepository extends JpaRepository<ChatMessageTemplateJpaEntity,Long> {
    List<ChatMessageTemplateJpaEntity> findByActiveTrueOrderByTypeAscIdAsc();
    List<ChatMessageTemplateJpaEntity> findByTypeAndActiveTrueOrderByIdAsc(ChatMessageTemplateJpaEntity.Type type);
}