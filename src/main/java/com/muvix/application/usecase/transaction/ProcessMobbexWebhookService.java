package com.muvix.application.usecase.transaction;

import com.muvix.application.port.in.transaction.ProcessMobbexWebhookUseCase;
import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.domain.model.catalogue.Batch;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.domain.model.transaction.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ProcessMobbexWebhookService implements ProcessMobbexWebhookUseCase {

    private final OrderRepositoryPort orderRepository;
    private final EscrowAccountRepositoryPort escrowRepository;
    private final SellableItemRepositoryPort itemRepository;
    private final MobbexGatewayPort mobbex;

    public ProcessMobbexWebhookService(OrderRepositoryPort orderRepository, EscrowAccountRepositoryPort escrowRepository,
                                       SellableItemRepositoryPort itemRepository, MobbexGatewayPort mobbex) {
        this.orderRepository = orderRepository;
        this.escrowRepository = escrowRepository;
        this.itemRepository = itemRepository;
        this.mobbex = mobbex;
    }

    @Transactional
    public void processWebhook(Map<String, Object> payload) {
        if (payload == null || !(payload.get("data") instanceof Map<?, ?> data)
                || !(data.get("payment") instanceof Map<?, ?> payment)) return;
        Map<?, ?> checkout = data.get("checkout") instanceof Map<?, ?> checkoutData ? checkoutData : Map.of();
        Object referenceValue = checkout.containsKey("reference") ? checkout.get("reference") : data.get("reference");
        Object operationValue = payment.get("id");
        Object paymentStatus = payment.get("status");
        Object statusCode = paymentStatus instanceof Map<?, ?> statusMap ? statusMap.get("code") : paymentStatus;
        if (referenceValue == null || operationValue == null) return;
        String internalReference = String.valueOf(referenceValue);
        String mobbexOperationId = String.valueOf(operationValue);
        if (!"200".equals(String.valueOf(statusCode))) return;
        Long orderId;
        try { orderId = Long.parseLong(internalReference); }
        catch (NumberFormatException exception) { return; }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("Pedido no encontrado: " + orderId));

        if (!mobbex.isApprovedOperation(internalReference, mobbexOperationId, order.getTotal()))
            throw new DomainException("Mobbex operation could not be verified");

        if ("200".equals(String.valueOf(statusCode))) {
            EscrowAccount escrow = escrowRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new DomainException("Escrow account not found"));

            if (escrow.getStatus() == EscrowAccount.EscrowStatus.CANCELLED) {
                mobbex.refundPayment(mobbexOperationId);
                escrow.refundLatePayment(mobbexOperationId);
                escrowRepository.save(escrow);
                return;
            }
            if (escrow.getStatus() == EscrowAccount.EscrowStatus.REFUNDED_TO_BUYER
                    && mobbexOperationId.equals(escrow.getExternalOperationId())) return;

            if (escrow.getStatus() == EscrowAccount.EscrowStatus.HELD
                    && mobbexOperationId.equals(escrow.getExternalOperationId())) return;
            if (escrow.getStatus() != EscrowAccount.EscrowStatus.PENDING_PAYMENT) {
                throw new DomainException("Escrow has already completed a different payment operation");
            }

            escrow.registerExternalOperationId(mobbexOperationId);

            // Se vincula la operación de Mobbex y los fondos quedan congelados (HELD)
            escrowRepository.save(escrow);

            order.getItemIds().forEach(itemId -> itemRepository.findById(itemId).ifPresent(item -> {
                if (item.getState() != SellableItem.State.SOLD) {
                    item.markAsSold();
                    itemRepository.save(item);
                }
                if (item instanceof Product) {
                    for (SellableItem candidate : itemRepository.findBySellerId(item.getSellerId())) {
                        if (candidate instanceof Batch batch && batch.contains(item.getId())) {
                            batch.removeProduct(item.getId());
                            itemRepository.save(batch);
                        }
                    }
                } else if (item instanceof Batch batch) {
                    batch.getProductIds().forEach(productId -> itemRepository.findById(productId).ifPresent(product -> {
                        if (product.getState() != SellableItem.State.SOLD) {
                            product.markAsSold();
                            itemRepository.save(product);
                        }
                    }));
                }
            }));

            // Transicionar pedido a listo para retiro
            orderRepository.save(order);
        }
    }
}
