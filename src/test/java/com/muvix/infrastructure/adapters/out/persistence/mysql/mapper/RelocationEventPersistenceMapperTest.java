package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.logistic.Location;
import com.muvix.domain.model.management.DiscountRule;
import com.muvix.domain.model.management.FullRelocationEvent;
import com.muvix.domain.model.management.PurchaseEvent;
import com.muvix.domain.model.management.RelocationEvent;
import com.muvix.domain.model.management.SaleEvent;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.RelocationEventJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RelocationEventPersistenceMapperTest {

    @Test
    void roundTripsSaleEventAndPreservesLinkedItemsOnUpdate() {
        Instant deadline = Instant.parse("2026-12-01T12:00:00Z");
        SaleEvent sale = new SaleEvent(
                12L, 34L, "Moving sale", new Location("zone-a", "street 1"),
                deadline, new BigDecimal("120.00"),
                List.of(new DiscountRule(3, new BigDecimal("15.00"))),
                "share-12", RelocationEvent.Status.ACTIVE, Set.of(56L));
        RelocationEventJpaEntity existing = new RelocationEventJpaEntity();
        existing.setItemIds(Set.of(90L));

        RelocationEventJpaEntity entity = RelocationEventPersistenceMapper.toEntity(sale, existing);
        RelocationEvent restored = RelocationEventPersistenceMapper.toDomain(entity);

        SaleEvent restoredSale = assertInstanceOf(SaleEvent.class, restored);
        assertEquals(12L, restoredSale.getId());
        assertEquals("street 1", restoredSale.getOriginLocation().exactAddress());
        assertEquals(deadline, restoredSale.getSaleDeadline());
        assertEquals(new BigDecimal("15.00"), restoredSale.getDiscountRules().getFirst().percentage());
        assertEquals(Set.of(56L), restoredSale.getSharedUserIds());
        assertEquals(Set.of(90L), entity.getItemIds());
    }

    @Test
    void roundTripsPurchaseAndFullRelocationTypes() {
        PurchaseEvent purchase = new PurchaseEvent(
                1L, 2L, "New home", new Location("zone-b", null),
                Instant.parse("2026-12-03T10:00:00Z"), "purchase-token",
                RelocationEvent.Status.DRAFT, Set.of());
        FullRelocationEvent full = new FullRelocationEvent(
                3L, 4L, "Relocation", new Location("zone-a", "origin"),
                new Location("zone-b", "destination"),
                Instant.parse("2026-12-01T10:00:00Z"), Instant.parse("2026-12-05T10:00:00Z"),
                new BigDecimal("250.00"), List.of(), "full-token",
                RelocationEvent.Status.DRAFT, Set.of());

        RelocationEvent restoredPurchase = RelocationEventPersistenceMapper.toDomain(
                RelocationEventPersistenceMapper.toEntity(purchase, null));
        RelocationEvent restoredFull = RelocationEventPersistenceMapper.toDomain(
                RelocationEventPersistenceMapper.toEntity(full, null));

        assertInstanceOf(PurchaseEvent.class, restoredPurchase);
        assertEquals("zone-b", ((PurchaseEvent) restoredPurchase).getDestinationLocation().zone());
        FullRelocationEvent fullResult = assertInstanceOf(FullRelocationEvent.class, restoredFull);
        assertEquals("destination", fullResult.getDestinationLocation().exactAddress());
        assertEquals(Set.of(), fullResult.getSharedUserIds());
    }
}
