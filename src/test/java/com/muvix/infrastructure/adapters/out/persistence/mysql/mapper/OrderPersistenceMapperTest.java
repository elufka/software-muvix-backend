package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.logistic.AuthorizedPickupPerson;
import com.muvix.domain.model.transaction.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderPersistenceMapperTest {

    @Test
    void roundTripsPickupSecurityDataAndDeliveredStatus() {
        Order order = new Order(null, 2L, 3L, Set.of(4L), new BigDecimal("25.00"),
                Order.Origin.DIRECT_PURCHASE, Order.DeliveryMethod.PICKUP, null,
                Order.Status.PENDING_SLOT);
        order.setAuthorizedPickupPerson(new AuthorizedPickupPerson("Pickup Person", "DOC-1", "555-0100"));
        order.confirmSlot(5L);
        order.completePickup(order.getPickupPin().pinCode());
        String pin = order.getPickupPin().pinCode();

        Order restored = OrderPersistenceMapper.toDomain(OrderPersistenceMapper.toEntity(order));

        assertEquals(order.getItemIds(), restored.getItemIds());
        assertEquals(Order.Status.DELIVERED, restored.getStatus());
        assertEquals(pin, restored.getPickupPin().pinCode());
        assertEquals("DOC-1", restored.getAuthorizedPickupPerson().documentId());
        assertEquals(order.getDeliveredAt(), restored.getDeliveredAt());
    }
}
