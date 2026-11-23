package com.muvix.application.usecase.logistic;

import com.muvix.application.port.in.logistic.SchedulePickupSlotUseCase;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.application.port.out.persistence.PickupSlotRepositoryPort;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.logistic.PickupSlot;
import com.muvix.domain.model.transaction.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SchedulePickupSlotService implements SchedulePickupSlotUseCase {
    private final OrderRepositoryPort orders;
    private final PickupSlotRepositoryPort slots;
    private final SellableItemRepositoryPort items;
    public SchedulePickupSlotService(OrderRepositoryPort orders, PickupSlotRepositoryPort slots,
                                     SellableItemRepositoryPort items) {
        this.orders = orders; this.slots = slots; this.items = items;
    }

    @Override @Transactional
    public PickupSlot proposeSlot(ProposeSlotCommand command) {
        Order order = getOrder(command.orderId());
        if (!order.getBuyerId().equals(command.buyerId())) throw new DomainException("Only the buyer can start pickup scheduling");
        if (order.getDeliveryMethod() != Order.DeliveryMethod.PICKUP || order.getStatus() != Order.Status.PENDING_SLOT)
            throw new DomainException("Order is not awaiting a pickup slot");
        if (order.getSlotId() != null) throw new DomainException("A pickup proposal already exists; use counter-propose");
        order.setAuthorizedPickupPerson(command.authorizedPerson());
        PickupSlot saved = slots.save(PickupSlot.requestSlot(order.getId(), command.startTime(), command.endTime()));
        order.attachPickupSlotProposal(saved.getId());
        orders.save(order);
        return saved;
    }

    @Override @Transactional(readOnly = true)
    public PickupSlot getSlot(Long slotId, Long userId) {
        PickupSlot slot = slots.findById(slotId).orElseThrow(() -> new DomainException("Pickup slot not found"));
        Order order = getOrder(slot.getOrderId());
        if (!order.getBuyerId().equals(userId) && !sellerId(order).equals(userId))
            throw new DomainException("Only order participants can read pickup scheduling");
        return slot;
    }

    @Override @Transactional
    public PickupSlot counterProposeSlot(CounterProposeSlotCommand command) {
        Order order = getOrder(command.orderId());
        Long sellerId = sellerId(order);
        if (!command.proposerId().equals(order.getBuyerId()) && !command.proposerId().equals(sellerId))
            throw new DomainException("Only the buyer or seller may counter the pickup proposal");
        PickupSlot slot = slots.findById(command.slotId()).filter(s -> s.getOrderId().equals(order.getId()))
                .orElseThrow(() -> new DomainException("Pickup slot not found for this order"));
        if (order.getStatus() != Order.Status.PENDING_SLOT) throw new DomainException("Order no longer accepts pickup proposals");
        if (!slot.getId().equals(order.getSlotId())) throw new DomainException("Only the current pickup proposal can be changed");
        slot.counterPropose(command.newStartTime(), command.newEndTime(), command.proposerId(), order.getBuyerId());
        return slots.save(slot);
    }

    @Override @Transactional
    public void confirmSlot(Long orderId, Long slotId, Long userId) {
        Order order = getOrder(orderId);
        Long sellerId = sellerId(order);
        if (order.getStatus() != Order.Status.PENDING_SLOT) throw new DomainException("Order is not awaiting a pickup slot");
        PickupSlot slot = slots.findById(slotId).filter(s -> s.getOrderId().equals(order.getId()))
                .orElseThrow(() -> new DomainException("Pickup slot not found for this order"));
        if (!slotId.equals(order.getSlotId())) throw new DomainException("Only the current pickup proposal can be confirmed");
        slot.confirm(userId, order.getBuyerId(), sellerId);
        PickupSlot saved = slots.save(slot);
        if (saved.isConfirmed()) {
            order.confirmSlot(saved.getId());
            orders.save(order);
        }
    }

    private Order getOrder(Long id) {
        return orders.findById(id).orElseThrow(() -> new DomainException("Order not found"));
    }
    private Long sellerId(Order order) {
        List<Long> sellers = order.getItemIds().stream().map(id -> items.findById(id)
                .orElseThrow(() -> new DomainException("Order item not found")))
                .map(SellableItem::getSellerId).distinct().toList();
        if (sellers.size() != 1) throw new DomainException("Order must belong to one seller");
        return sellers.get(0);
    }
}
