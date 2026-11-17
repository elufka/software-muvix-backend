package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.ShoppingCartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ShoppingCartJpaRepository extends JpaRepository<ShoppingCartJpaEntity,Long> {}
