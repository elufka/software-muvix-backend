package com.muvix.application.usecase.logistic;

import com.muvix.application.port.in.logistic.CompletePickupUseCase;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.application.port.out.persistence.PickupSlotRepositoryPort;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.application.port.out.security.PickupPinHashPort;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.Order;
import com.muvix.domain.model.transaction.EscrowAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompletePickupService implements CompletePickupUseCase {
    private final OrderRepositoryPort orders;
    private final PickupSlotRepositoryPort slots;
    private final SellableItemRepositoryPort items;
    private final EscrowAccountRepositoryPort escrows;
    private final PickupPinHashPort pinHasher;

    public CompletePickupService(OrderRepositoryPort orders, PickupSlotRepositoryPort slots,
                                 SellableItemRepositoryPort items, EscrowAccountRepositoryPort escrows,
                                 PickupPinHashPort pinHasher) {
        this.orders = orders;
        this.slots = slots;
        this.items = items;
        this.escrows = escrows;
        this.pinHasher = pinHasher;
    }

    @Override
    @Transactional
    public void complete(Long orderId, Long sellerId, String pickupPin) {
        Order order = orders.findById(orderId).orElseThrow(() -> new DomainException("Order not found"));
        List<Long> sellers = order.getItemIds().stream().map(items::findById)
                .flatMap(java.util.Optional::stream).map(SellableItem::getSellerId).distinct().toList();
        if (sellers.size() != 1 || !sellers.get(0).equals(sellerId))
            throw new DomainException("Only the seller can verify pickup for this order");
        if (order.getSlotId() == null || slots.findById(order.getSlotId())
                .filter(slot -> slot.getOrderId().equals(orderId) && slot.isConfirmed()).isEmpty())
            throw new DomainException("Order has no confirmed pickup slot");
        EscrowAccount escrow = escrows.findByOrderId(orderId)
                .orElseThrow(() -> new DomainException("Escrow not found"));
        if (escrow.getStatus() != EscrowAccount.EscrowStatus.HELD)
            throw new DomainException("Pickup can only be completed after payment is held");
        if (!pinHasher.matches(pickupPin, order.getPickupPinHash()))
            throw new DomainException("Invalid verification PIN for pickup");
        order.completePickup();
        orders.save(order);
    }
}