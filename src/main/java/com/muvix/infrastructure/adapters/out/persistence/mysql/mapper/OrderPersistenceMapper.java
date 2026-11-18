package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.logistic.AuthorizedPickupPerson;
import com.muvix.domain.model.transaction.Order;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.AuthorizedPickupPersonEmbeddable;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.OrderJpaEntity;

public final class OrderPersistenceMapper {

    private OrderPersistenceMapper() {}

    public static Order toDomain(OrderJpaEntity entity) {
        if (entity == null) return null;
        AuthorizedPickupPersonEmbeddable persistedPerson = entity.getAuthorizedPerson();
        AuthorizedPickupPerson person = persistedPerson == null ? null
                : new AuthorizedPickupPerson(persistedPerson.getFullName(),
                        persistedPerson.getDocumentId(), persistedPerson.getPhone());
        return Order.restore(
                entity.getId(),
                entity.getEventId(),
                entity.getBuyerId(),
                entity.getItemIds(),
                entity.getSourceBatchIds(),
                entity.getTotal(),
                Order.Origin.valueOf(entity.getOrigin().name()),
                Order.DeliveryMethod.valueOf(entity.getDeliveryMethod().name()),
                entity.getSlotId(),
                Order.Status.valueOf(entity.getStatus().name()),
                person,
                entity.getPickupPin(),
                entity.getDeliveredAt()
        );
    }

    public static OrderJpaEntity toEntity(Order order) {
        if (order == null) return null;
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(order.getId());
        entity.setEventId(order.getEventId());
        entity.setBuyerId(order.getBuyerId());
        entity.setItemIds(new java.util.HashSet<>(order.getItemIds()));
        entity.setSourceBatchIds(new java.util.HashSet<>(order.getSourceBatchIds()));
        entity.setTotal(order.getTotal());
        entity.setOrigin(OrderJpaEntity.OriginJpa.valueOf(order.getOrigin().name()));
        entity.setDeliveryMethod(OrderJpaEntity.DeliveryMethodJpa.valueOf(order.getDeliveryMethod().name()));
        entity.setStatus(OrderJpaEntity.StatusJpa.valueOf(order.getStatus().name()));
        entity.setSlotId(order.getSlotId());
        entity.setDeliveredAt(order.getDeliveredAt());
        if (order.getAuthorizedPickupPerson() == null) {
            entity.setAuthorizedPerson(null);
        } else {
            AuthorizedPickupPerson person = order.getAuthorizedPickupPerson();
            AuthorizedPickupPersonEmbeddable persistedPerson = new AuthorizedPickupPersonEmbeddable();
            persistedPerson.setFullName(person.fullName());
            persistedPerson.setDocumentId(person.documentId());
            persistedPerson.setPhone(person.phone());
            entity.setAuthorizedPerson(persistedPerson);
        }
        entity.setPickupPin(order.getPickupPin() == null ? null : order.getPickupPin().pinCode());
        return entity;
    }
}
