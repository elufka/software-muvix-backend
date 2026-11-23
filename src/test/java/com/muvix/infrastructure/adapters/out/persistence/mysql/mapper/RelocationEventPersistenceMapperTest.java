package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.logistic.Location;
import com.muvix.domain.model.management.DiscountRule;
import com.muvix.domain.model.management.FullRelocationBox;
import com.muvix.domain.model.management.PurchaseBox;
import com.muvix.domain.model.management.Box;
import com.muvix.domain.model.management.SaleBox;
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
        SaleBox sale = new SaleBox(
                12L, 34L, "Moving sale", new Location("zone-a", "street 1"),
                deadline, new BigDecimal("120.00"),
                List.of(new DiscountRule(3, new BigDecimal("15.00"))),
                "share-12", Box.Status.ACTIVE, Set.of(56L));
        RelocationEventJpaEntity existing = new RelocationEventJpaEntity();
        existing.setItemIds(Set.of(90L));

        RelocationEventJpaEntity entity = RelocationEventPersistenceMapper.toEntity(sale, existing);
        Box restored = RelocationEventPersistenceMapper.toDomain(entity);

        SaleBox restoredSale = assertInstanceOf(SaleBox.class, restored);
        assertEquals(12L, restoredSale.getId());
        assertEquals("street 1", restoredSale.getOriginLocation().exactAddress());
        assertEquals(deadline, restoredSale.getSaleDeadline());
        assertEquals(new BigDecimal("15.00"), restoredSale.getDiscountRules().getFirst().percentage());
        assertEquals(Set.of(56L), restoredSale.getSharedUserIds());
        assertEquals(Set.of(90L), entity.getItemIds());
    }

    @Test
    void roundTripsPurchaseAndFullRelocationTypes() {
        PurchaseBox purchase = new PurchaseBox(
                1L, 2L, "New home", new Location("zone-b", null),
                Instant.parse("2026-12-03T10:00:00Z"), "purchase-token",
                Box.Status.DRAFT, Set.of());
        FullRelocationBox full = new FullRelocationBox(
                3L, 4L, "Relocation", new Location("zone-a", "origin"),
                new Location("zone-b", "destination"),
                Instant.parse("2026-12-01T10:00:00Z"), Instant.parse("2026-12-05T10:00:00Z"),
                new BigDecimal("250.00"), List.of(), "full-token",
                Box.Status.DRAFT, Set.of());

        Box restoredPurchase = RelocationEventPersistenceMapper.toDomain(
                RelocationEventPersistenceMapper.toEntity(purchase, null));
        Box restoredFull = RelocationEventPersistenceMapper.toDomain(
                RelocationEventPersistenceMapper.toEntity(full, null));

        assertInstanceOf(PurchaseBox.class, restoredPurchase);
        assertEquals("zone-b", ((PurchaseBox) restoredPurchase).getDestinationLocation().zone());
        FullRelocationBox fullResult = assertInstanceOf(FullRelocationBox.class, restoredFull);
        assertEquals("destination", fullResult.getDestinationLocation().exactAddress());
        assertEquals(Set.of(), fullResult.getSharedUserIds());
    }
}
