package com.muvix.application.usecase.transaction;

import com.muvix.application.port.in.transaction.RefundOrderUseCase;
import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.EscrowAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundOrderService implements RefundOrderUseCase {

    private final EscrowAccountRepositoryPort escrowRepository;
    private final MobbexGatewayPort mobbexGateway;

    public RefundOrderService(EscrowAccountRepositoryPort escrowRepository, MobbexGatewayPort mobbexGateway) {
        this.escrowRepository = escrowRepository;
        this.mobbexGateway = mobbexGateway;
    }

    @Transactional
    public void refundOrder(Long orderId, String mobbexOperationId) {
        EscrowAccount escrow = escrowRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DomainException("Cuenta Escrow no encontrada"));

        // 1. Invocar devolución en Mobbex
        mobbexGateway.refundPayment(mobbexOperationId);

        // 2. Actualizar estado de retención
        escrow.refundToBuyer();
        escrowRepository.save(escrow);
    }
}