package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.OfferJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OfferJpaRepository extends JpaRepository<OfferJpaEntity, Long> {}
