package com.muvix.application.usecase.transaction;

import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.domain.model.transaction.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ReleaseEscrowService {

    private final OrderRepositoryPort orderRepository;
    private final EscrowAccountRepositoryPort escrowRepository;
    private final MobbexGatewayPort mobbexGateway;

    public ReleaseEscrowService(OrderRepositoryPort orderRepository,
                                EscrowAccountRepositoryPort escrowRepository,
                                MobbexGatewayPort mobbexGateway) {
        this.orderRepository = orderRepository;
        this.escrowRepository = escrowRepository;
        this.mobbexGateway = mobbexGateway;
    }

    @Transactional
    public void releaseFunds(Long orderId, String mobbexOperationId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("Pedido no encontrado"));

        EscrowAccount escrow = escrowRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DomainException("Cuenta Escrow no encontrada"));

        // 1. Invocación a Mobbex para acreditar al CUIT del vendedor
        mobbexGateway.releaseFunds(mobbexOperationId);

        // 2. Transición del modelo de dominio
        escrow.releaseToSeller(order.getDeliveredAt(), Instant.now());
        escrowRepository.save(escrow);
    }
}