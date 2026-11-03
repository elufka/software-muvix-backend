package com.muvix.application.usecase.transaction;

import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.domain.model.transaction.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ProcessMobbexWebhookService {

    private final OrderRepositoryPort orderRepository;
    private final EscrowAccountRepositoryPort escrowRepository;

    public ProcessMobbexWebhookService(OrderRepositoryPort orderRepository, EscrowAccountRepositoryPort escrowRepository) {
        this.orderRepository = orderRepository;
        this.escrowRepository = escrowRepository;
    }

    @Transactional
    public void processWebhook(Map<String, Object> payload) {
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        Map<String, Object> payment = (Map<String, Object>) data.get("payment");

        String internalReference = (String) data.get("reference");
        String mobbexOperationId = (String) data.get("id");
        String statusMobbex = String.valueOf(payment.get("status"));

        Long orderId = Long.parseLong(internalReference);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("Pedido no encontrado: " + orderId));

        if ("approved".equalsIgnoreCase(statusMobbex) || "200".equals(statusMobbex)) {
            // Actualizar cuenta de custodia (Escrow)
            EscrowAccount escrow = escrowRepository.findByOrderId(orderId)
                    .orElseGet(() -> new EscrowAccount(orderId, order.getTotal()));

            // Se vincula la operación de Mobbex y los fondos quedan congelados (HELD)
            escrowRepository.save(escrow);

            // Transicionar pedido a listo para retiro
            orderRepository.save(order);
        }
    }
}