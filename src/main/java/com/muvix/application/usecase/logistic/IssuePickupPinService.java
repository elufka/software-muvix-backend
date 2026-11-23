package com.muvix.application.usecase.logistic;

import com.muvix.application.port.in.logistic.IssuePickupPinUseCase;
import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.application.port.out.persistence.PickupSlotRepositoryPort;
import com.muvix.application.port.out.security.PickupPinHashPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.logistic.PickupPin;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.domain.model.transaction.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IssuePickupPinService implements IssuePickupPinUseCase {
    private final OrderRepositoryPort orders;
    private final PickupSlotRepositoryPort slots;
    private final EscrowAccountRepositoryPort escrows;
    private final PickupPinHashPort pinHasher;

    public IssuePickupPinService(OrderRepositoryPort orders, PickupSlotRepositoryPort slots,
                                 EscrowAccountRepositoryPort escrows, PickupPinHashPort pinHasher) {
        this.orders = orders;
        this.slots = slots;
        this.escrows = escrows;
        this.pinHasher = pinHasher;
    }

    @Override
    @Transactional
    public String issue(Long orderId, Long buyerId) {
        Order order = orders.findById(orderId).orElseThrow(() -> new DomainException("Order not found"));
        if (!order.getBuyerId().equals(buyerId)) throw new DomainException("Only the buyer may request the pickup PIN");
        if (order.getStatus() != Order.Status.SLOT_CONFIRMED || order.getAuthorizedPickupPerson() == null)
            throw new DomainException("A confirmed pickup slot and authorized person are required before issuing a PIN");
        if (order.getSlotId() == null || slots.findById(order.getSlotId())
                .filter(slot -> slot.getOrderId().equals(orderId) && slot.isConfirmed()).isEmpty())
            throw new DomainException("Order has no confirmed pickup slot");
        EscrowAccount escrow = escrows.findByOrderId(orderId)
                .orElseThrow(() -> new DomainException("Escrow not found"));
        if (escrow.getStatus() != EscrowAccount.EscrowStatus.HELD)
            throw new DomainException("Pickup PIN can only be issued after payment is held");

        String rawPin = PickupPin.generate().pinCode();
        order.assignPickupPinHash(pinHasher.hash(rawPin));
        orders.save(order);
        return rawPin;
    }
}
