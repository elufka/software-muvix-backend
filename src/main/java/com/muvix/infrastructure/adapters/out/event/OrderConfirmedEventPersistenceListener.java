package com.muvix.infrastructure.adapters.out.event;

import com.muvix.domain.model.event.OrderConfirmedEvent;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.OrderConfirmedEventJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.OrderConfirmedEventJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderConfirmedEventPersistenceListener {
    private final OrderConfirmedEventJpaRepository repository;

    public OrderConfirmedEventPersistenceListener(OrderConfirmedEventJpaRepository repository) {
        this.repository = repository;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void persist(OrderConfirmedEvent event) {
        OrderConfirmedEventJpaEntity row = new OrderConfirmedEventJpaEntity();
        row.setOrderId(event.getOrderId());
        row.setBuyerName(event.getBuyerName());
        row.setSellerName(event.getSellerName());
        row.setEventId(event.getEventId());
        row.setTotalAmount(event.getTotalAmount());
        row.setOccurredOn(event.getOccurredOn());
        repository.save(row);
    }
}