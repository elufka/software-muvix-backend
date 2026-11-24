package com.muvix.application.usecase.transaction;

import com.muvix.application.port.in.transaction.CancelOrderUseCase;
import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.application.port.out.persistence.*;
import com.muvix.domain.model.catalogue.Batch;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.domain.model.transaction.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelOrderService implements CancelOrderUseCase {
    private final OrderRepositoryPort orders;
    private final EscrowAccountRepositoryPort escrows;
    private final PickupSlotRepositoryPort slots;
    private final SellableItemRepositoryPort items;
    private final MobbexGatewayPort mobbex;

    public CancelOrderService(OrderRepositoryPort orders, EscrowAccountRepositoryPort escrows,
                              PickupSlotRepositoryPort slots, SellableItemRepositoryPort items,
                              MobbexGatewayPort mobbex) {
        this.orders=orders; this.escrows=escrows; this.slots=slots; this.items=items; this.mobbex=mobbex;
    }

    @Override @Transactional
    public void cancelByBuyer(Long orderId, Long buyerId) {
        Order order=orders.findById(orderId).orElseThrow(()->new DomainException("Order not found"));
        if(!order.getBuyerId().equals(buyerId))throw new DomainException("Only the buyer can cancel this order");
        cancel(order);
    }

    @Override @Transactional
    public boolean cancelExpiredPendingSlot(Long orderId,java.time.Instant createdBefore){
        Order order=orders.findById(orderId).orElseThrow(()->new DomainException("Order not found"));
        if(order.getStatus()!=Order.Status.PENDING_SLOT||order.getCreatedAt().isAfter(createdBefore))return false;
        cancel(order);
        return true;
    }

    private void cancel(Order order){
        Long orderId=order.getId();
        if(slots.findLatestByOrderId(orderId).map(slot -> slot.isSellerConfirmed()).orElse(false))
            throw new DomainException("Order cannot be cancelled after the seller confirms pickup");
        order.cancelBeforeSellerPickup();

        EscrowAccount escrow=escrows.findByOrderId(orderId).orElseThrow(()->new DomainException("Escrow not found"));
        if(escrow.getStatus()==EscrowAccount.EscrowStatus.PENDING_PAYMENT) {
            escrow.cancelUnpaid();
        } else if(escrow.getStatus()==EscrowAccount.EscrowStatus.HELD) {
            if(escrow.getExternalOperationId()==null)throw new DomainException("Paid order has no Mobbex operation");
            mobbex.refundPayment(escrow.getExternalOperationId());
            escrow.refundToBuyer();
        } else {
            throw new DomainException("Order escrow cannot be cancelled in its current state");
        }

        restoreInventory(order);
        escrows.save(escrow);
        orders.save(order);
    }

    private void restoreInventory(Order order) {
        for(Long itemId:order.getItemIds()) {
            SellableItem item=items.findById(itemId).orElseThrow(()->new DomainException("Order item not found"));
            restoreIfUnavailable(item);
            if(item instanceof Batch batch) {
                for(Long productId:batch.getProductIds()) {
                    SellableItem product=items.findById(productId).orElseThrow(()->new DomainException("Batch product not found"));
                    restoreIfUnavailable(product);
                }
            } else if(item instanceof Product product) {
                for(Long batchId:order.getSourceBatchIds()) {
                    items.findById(batchId).filter(Batch.class::isInstance).map(Batch.class::cast).ifPresent(batch -> {
                        batch.restoreProductAfterRefund(product);
                        items.save(batch);
                    });
                }
            }
        }
    }

    private void restoreIfUnavailable(SellableItem item) {
        if(item.getState()==SellableItem.State.RESERVED || item.getState()==SellableItem.State.SOLD) {
            item.restoreAfterRefund();
            items.save(item);
        }
    }
}