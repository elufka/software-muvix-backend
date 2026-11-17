package com.muvix.application.usecase.transaction;

import com.muvix.application.port.in.transaction.OpenDisputeUseCase;
import com.muvix.application.port.out.event.EventPublisherPort;
import com.muvix.application.port.out.persistence.DisputeRepositoryPort;
import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.domain.model.event.DisputeOpenedEvent;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.Dispute;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.domain.model.transaction.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class OpenDisputeService implements OpenDisputeUseCase {

    private final OrderRepositoryPort orderRepository;
    private final DisputeRepositoryPort disputeRepository;
    private final EscrowAccountRepositoryPort escrowRepository;
    private final EventPublisherPort eventPublisher;
    private final Clock clock;

    public OpenDisputeService(OrderRepositoryPort orderRepository,
                              DisputeRepositoryPort disputeRepository,
                              EscrowAccountRepositoryPort escrowRepository,
                              EventPublisherPort eventPublisher,
                              Clock clock) {
        this.orderRepository = orderRepository;
        this.disputeRepository = disputeRepository;
        this.escrowRepository = escrowRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Dispute openDispute(OpenDisputeCommand command) {
        if (command == null) throw new DomainException("Dispute details are required");
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new DomainException("Order not found"));
        if (!order.getBuyerId().equals(command.buyerId())) {
            throw new DomainException("Only the buyer can open a dispute for this order");
        }
        if (order.getStatus() != Order.Status.DELIVERED || order.getDeliveredAt() == null) {
            throw new DomainException("A dispute can only be opened for a delivered order");
        }
        if (disputeRepository.findByOrderId(order.getId()).isPresent()) {
            throw new DomainException("A dispute already exists for this order");
        }

        EscrowAccount escrow = escrowRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new DomainException("Escrow account not found"));
        escrow.markDisputed();

        Dispute dispute = new Dispute(null, order.getId(), command.buyerId(), command.reason(),
                command.evidenceImages(), order.getDeliveredAt(), clock.instant());
        Dispute saved = disputeRepository.save(dispute);
        escrowRepository.save(escrow);
        eventPublisher.publish(new DisputeOpenedEvent(saved.getId(), saved.getOrderId(),
                saved.getBuyerId(), saved.getReason()));
        return saved;
    }
}
