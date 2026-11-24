package com.muvix.application.usecase.transaction;

import com.muvix.application.port.in.transaction.CreateOrderCheckoutUseCase;
import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.application.port.out.persistence.*;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.*;
import com.muvix.domain.model.logistic.PickupSlot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOrderCheckoutService implements CreateOrderCheckoutUseCase {
    private final OrderRepositoryPort orders; private final PickupSlotRepositoryPort slots;
    private final EscrowAccountRepositoryPort escrows; private final MobbexGatewayPort mobbex;
    public CreateOrderCheckoutService(OrderRepositoryPort orders,PickupSlotRepositoryPort slots,EscrowAccountRepositoryPort escrows,MobbexGatewayPort mobbex){this.orders=orders;this.slots=slots;this.escrows=escrows;this.mobbex=mobbex;}
    @Override @Transactional public String create(Long orderId,Long buyerId){
        Order order=orders.findById(orderId).orElseThrow(()->new DomainException("Order not found"));
        if(!order.getBuyerId().equals(buyerId))throw new DomainException("Only the buyer can initiate checkout");
        if(order.getStatus()!=Order.Status.SLOT_CONFIRMED||order.getSlotId()==null||slots.findById(order.getSlotId()).filter(PickupSlot::isConfirmed).isEmpty())
            throw new DomainException("Pickup time must be confirmed before checkout");
        EscrowAccount escrow=escrows.findByOrderId(orderId).orElseThrow(()->new DomainException("Escrow not found"));
        if(escrow.getStatus()!=EscrowAccount.EscrowStatus.PENDING_PAYMENT)throw new DomainException("Order is no longer awaiting payment");
        if(escrow.getCheckoutUrl()!=null)return escrow.getCheckoutUrl();
        var payment=mobbex.createCheckout(new MobbexGatewayPort.CheckoutRequest(orderId.toString(),order.getTotal(),"Muvix order "+orderId,""));
        if (payment == null || payment.mobbexPaymentId() == null || payment.mobbexPaymentId().isBlank()
                || payment.checkoutUrl() == null || payment.checkoutUrl().isBlank()) {
            throw new DomainException("Mobbex did not return a valid checkout session");
        }
        escrow.attachCheckout(payment.mobbexPaymentId(),payment.checkoutUrl());escrows.save(escrow);
        return payment.checkoutUrl();
    }
}