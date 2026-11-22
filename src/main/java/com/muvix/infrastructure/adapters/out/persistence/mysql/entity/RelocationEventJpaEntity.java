    package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

    import com.muvix.domain.model.management.enums.BoxRole;
    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.Setter;

    import java.math.BigDecimal;
    import java.time.Instant;
    import java.util.*;

    @Entity
    @Table(name = "relocation_events")
    @Getter @Setter
    public class RelocationEventJpaEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Enumerated(EnumType.STRING)
        @Column(name = "event_type", nullable = false, length = 32)
        private EventTypeJpa eventType;

        @Column(name = "user_id", nullable = false)
        private Long userId;

        @Column(nullable = false)
        private String title;

        @Column(name = "share_token", nullable = false, unique = true, length = 36)
        private String shareToken;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 24)
        private StatusJpa status;

        @Column(name = "origin_zone")
        private String originZone;

        @Column(name = "origin_exact_address", columnDefinition = "TEXT")
        private String originExactAddress;

        @Column(name = "destination_zone")
        private String destinationZone;

        @Column(name = "destination_exact_address", columnDefinition = "TEXT")
        private String destinationExactAddress;

        @Column(name = "sale_deadline")
        private Instant saleDeadline;

        @Column(name = "arrival_delivery_date")
        private Instant arrivalDeliveryDate;

        @Column(name = "take_it_all_price", precision = 12, scale = 2)
        private BigDecimal takeItAllPrice;

        @ElementCollection
        @CollectionTable(name = "relocation_event_discount_rules", joinColumns = @JoinColumn(name = "event_id"))
        @OrderColumn(name = "rule_order")
        private List<DiscountRuleJpaValue> discountRules = new ArrayList<>();

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(
                name = "relocation_event_participants",
                joinColumns = @JoinColumn(name = "event_id")
        )
        @MapKeyColumn(name = "user_id")
        @Column(name = "role", nullable = false)
        @Enumerated(EnumType.STRING)
        private Map<Long, BoxRole> participants = new HashMap<>();

        @ElementCollection
        @CollectionTable(name = "relocation_event_items", joinColumns = @JoinColumn(name = "event_id"))
        @Column(name = "item_id", nullable = false)
        private Set<Long> itemIds = new HashSet<>();

        public enum EventTypeJpa { SALE, PURCHASE, FULL_RELOCATION }
        public enum StatusJpa { DRAFT, ACTIVE, COMPLETED, CANCELLED }

    }
