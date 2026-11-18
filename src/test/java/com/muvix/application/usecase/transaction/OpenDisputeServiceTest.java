package com.muvix.application.usecase.transaction;

import com.muvix.application.port.in.transaction.OpenDisputeUseCase;
import com.muvix.application.port.out.event.EventPublisherPort;
import com.muvix.application.port.out.persistence.DisputeRepositoryPort;
import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.domain.model.event.DomainEvent;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.Dispute;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.domain.model.transaction.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenDisputeServiceTest {

    @Test
    void opensDisputeForDeliveredBuyerOrderAndHoldsEscrow() {
        Instant now = Instant.parse("2026-11-17T12:00:00Z");
        Order deliveredOrder = Order.restore(11L, 22L, 33L, Set.of(44L), new BigDecimal("10.00"),
                Order.Origin.DIRECT_PURCHASE, Order.DeliveryMethod.PICKUP, 55L, Order.Status.DELIVERED,
                null, null, now.minusSeconds(600));
        FakeOrders orders = new FakeOrders(deliveredOrder);
        FakeDisputes disputes = new FakeDisputes();
        FakeEscrows escrows = new FakeEscrows(paidEscrow(11L, new BigDecimal("10.00")));
        FakeEvents events = new FakeEvents();
        OpenDisputeService service = new OpenDisputeService(orders, disputes, escrows, events,
                Clock.fixed(now, ZoneOffset.UTC));

        Dispute result = service.openDispute(new OpenDisputeUseCase.OpenDisputeCommand(
                11L, 33L, "Item arrived damaged", List.of("evidence.jpg")));

        assertEquals(99L, result.getId());
        assertEquals(Dispute.Status.OPENED, result.getStatus());
        assertEquals(EscrowAccount.EscrowStatus.DISPUTED, escrows.account.getStatus());
        assertEquals(1, events.published.size());
    }

    @Test
    void rejectsNonBuyerBeforeMutatingEscrow() {
        Instant now = Instant.now();
        Order order = Order.restore(11L, 22L, 33L, Set.of(44L), BigDecimal.TEN,
                Order.Origin.DIRECT_PURCHASE, Order.DeliveryMethod.PICKUP, 55L, Order.Status.DELIVERED,
                null, null, now.minusSeconds(60));
        FakeEscrows escrows = new FakeEscrows(paidEscrow(11L, BigDecimal.TEN));
        OpenDisputeService service = new OpenDisputeService(new FakeOrders(order), new FakeDisputes(),
                escrows, new FakeEvents(), Clock.systemUTC());

        assertThrows(DomainException.class, () -> service.openDispute(
                new OpenDisputeUseCase.OpenDisputeCommand(11L, 7L, "reason", List.of())));
        assertEquals(EscrowAccount.EscrowStatus.HELD, escrows.account.getStatus());
    }

    private static EscrowAccount paidEscrow(Long orderId, BigDecimal amount) {
        EscrowAccount account = new EscrowAccount(orderId, amount);
        account.registerExternalOperationId("mobbex-" + orderId);
        return account;
    }

    private static final class FakeOrders implements OrderRepositoryPort {
        private final Order order;
        private FakeOrders(Order order) { this.order = order; }
        @Override public Order save(Order value) { return value; }
        @Override public Optional<Order> findById(Long id) { return Optional.of(order); }
        @Override public List<Order> findDeliveredBefore(Instant deliveredBefore) { return List.of(); }
    }

    private static final class FakeDisputes implements DisputeRepositoryPort {
        @Override public Dispute save(Dispute dispute) {
            return Dispute.restore(99L, dispute.getOrderId(), dispute.getBuyerId(), dispute.getReason(),
                    dispute.getEvidenceImages(), dispute.getStatus(), dispute.getCreatedAt());
        }
        @Override public Optional<Dispute> findById(Long id) { return Optional.empty(); }
        @Override public Optional<Dispute> findByOrderId(Long orderId) { return Optional.empty(); }
        @Override public List<Dispute> findByBuyerId(Long buyerId) { return List.of(); }
        @Override public List<Dispute> findByStatus(Dispute.Status status) { return List.of(); }
    }

    private static final class FakeEscrows implements EscrowAccountRepositoryPort {
        private final EscrowAccount account;
        private FakeEscrows(EscrowAccount account) { this.account = account; }
        @Override public EscrowAccount save(EscrowAccount value) { return value; }
        @Override public Optional<EscrowAccount> findByOrderId(Long orderId) { return Optional.of(account); }
        @Override public List<EscrowAccount> findPendingReleaseAccounts(Instant deliveredBefore) { return List.of(); }
        @Override public List<EscrowAccount> findByStatus(EscrowAccount.EscrowStatus status) { return List.of(); }
    }

    private static final class FakeEvents implements EventPublisherPort {
        private final List<DomainEvent> published = new java.util.ArrayList<>();
        @Override public void publish(DomainEvent event) { published.add(event); }
        @Override public void publishAll(List<DomainEvent> events) { published.addAll(events); }
    }
}
