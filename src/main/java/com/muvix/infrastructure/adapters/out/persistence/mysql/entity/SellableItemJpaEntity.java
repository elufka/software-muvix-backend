package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.StateJpa;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name = "sellable_items")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class SellableItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price; // 0.00 = Private item

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StateJpa state;

    @Column(nullable = false)
    private String category;

    @Version private Long version;



}
