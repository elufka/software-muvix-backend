package com.muvix.infrastructure.adapters.in.rest.dto.response;

import com.muvix.domain.model.logistic.AuthorizedPickupPerson;
import com.muvix.domain.model.transaction.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record OrderResponse(
        Long id,
        Long eventId,
        Long buyerId,
        String buyerName,
        String sellerName,
        Set<Long> itemIds,
        BigDecimal total,
        Order.Origin origin,
        Order.DeliveryMethod deliveryMethod,
        Long slotId,
        Order.Status status,
        AuthorizedPickupPerson authorizedPickupPerson,
        Instant deliveredAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getEventId(),
                order.getBuyerId(),
                order.getBuyerName(),
                order.getSellerName(),
                order.getItemIds(),
                order.getTotal(),
                order.getOrigin(),
                order.getDeliveryMethod(),
                order.getSlotId(),
                order.getStatus(),
                order.getAuthorizedPickupPerson(),
                order.getDeliveredAt());
    }
}