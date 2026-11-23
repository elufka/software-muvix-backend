package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.logistic.AuthorizedPickupPerson;
import com.muvix.domain.model.transaction.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderPersistenceMapperTest {

    @Test
    void roundTripsPickupSecurityDataAndConfirmedStatus() {
        Order order = new Order(null, 2L, 3L, "Buyer Name", "Seller Name", Set.of(4L), new BigDecimal("25.00"),
                Order.Origin.DIRECT_PURCHASE, Order.DeliveryMethod.PICKUP, null,
                Order.Status.PENDING_SLOT);
        order.setAuthorizedPickupPerson(new AuthorizedPickupPerson("Pickup Person", "DOC-1", "555-0100"));
        order.confirmSlot(5L);
        String pinHash = "$2a$12$test-hash-for-pickup-pin";
        order.assignPickupPinHash(pinHash);

        Order restored = OrderPersistenceMapper.toDomain(OrderPersistenceMapper.toEntity(order));

        assertEquals(order.getItemIds(), restored.getItemIds());
        assertEquals("Buyer Name", restored.getBuyerName());
        assertEquals("Seller Name", restored.getSellerName());
        assertEquals(Order.Status.SLOT_CONFIRMED, restored.getStatus());
        assertEquals(pinHash, restored.getPickupPinHash());
        assertEquals("Buyer Name", restored.getBuyerName());
        assertEquals("DOC-1", restored.getAuthorizedPickupPerson().documentId());
        assertEquals(order.getDeliveredAt(), restored.getDeliveredAt());
    }
}
