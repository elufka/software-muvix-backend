package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.ChatMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageJpaEntity,Long> {
    List<ChatMessageJpaEntity> findByOrderIdOrderByCreatedAtAscIdAsc(Long orderId);
    List<ChatMessageJpaEntity> findByOfferIdOrderByCreatedAtAscIdAsc(Long offerId);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("update ChatMessageJpaEntity m set m.orderId=:orderId where m.offerId=:offerId")
    void linkOfferMessages(@org.springframework.data.repository.query.Param("offerId") Long offerId,@org.springframework.data.repository.query.Param("orderId") Long orderId);
}