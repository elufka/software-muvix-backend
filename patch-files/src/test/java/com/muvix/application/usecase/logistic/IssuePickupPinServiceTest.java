package com.muvix.application.usecase.logistic;

import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.application.port.out.persistence.PickupSlotRepositoryPort;
import com.muvix.application.port.out.security.PickupPinHashPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.logistic.AuthorizedPickupPerson;
import com.muvix.domain.model.logistic.PickupSlot;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.domain.model.transaction.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IssuePickupPinServiceTest {
    @Test
    void buyerReceivesPinOnlyFromExplicitRequestAndOnlyHashIsStored() {
        Fixture fixture = new Fixture();

        String pin = fixture.service.issue(11L, 7L);

        assertTrue(pin.matches("\\d{6}"));
        assertNotEquals(pin, fixture.order.getPickupPinHash());
        assertTrue(fixture.hasher.matches(pin, fixture.order.getPickupPinHash()));
        assertEquals(1, fixture.orders.saveCount);
    }

    @Test
    void onlyTheBuyerCanRequestPin() {
        Fixture fixture = new Fixture();

        assertThrows(DomainException.class, () -> fixture.service.issue(11L, 8L));
        assertNull(fixture.order.getPickupPinHash());
        assertEquals(0, fixture.orders.saveCount);
    }

    private static final class Fixture {
        private final Order order = Order.restore(11L, null, 7L, "Buyer", "Seller", Set.of(55L), Set.of(),
                BigDecimal.TEN, Order.Origin.DIRECT_PURCHASE, Order.DeliveryMethod.PICKUP, 22L,
                Order.Status.SLOT_CONFIRMED, new AuthorizedPickupPerson("Buyer", "DOC-1", "555"), null, null);
        private final FakeOrders orders = new FakeOrders(order);
        private final FakeHasher hasher = new FakeHasher();
        private final IssuePickupPinService service = new IssuePickupPinService(orders, new FakeSlots(),
                new FakeEscrows(), hasher);
    }

    private static final class FakeOrders implements OrderRepositoryPort {
        private final Order order;
        private int saveCount;
        private FakeOrders(Order order) { this.order = order; }
        @Override public Order save(Order value) { saveCount++; return value; }
        @Override public Optional<Order> findById(Long id) { return Optional.of(order); }
        @Override public List<Order> findDeliveredBefore(Instant at) { return List.of(); }
        @Override public List<Order> findPendingSlotBefore(Instant createdBefore) { return List.of(); }
    }

    private static final class FakeSlots implements PickupSlotRepositoryPort {
        private final PickupSlot slot = PickupSlot.restore(22L, 11L, Instant.parse("2026-11-23T10:00:00Z"),
                Instant.parse("2026-11-23T11:00:00Z"), PickupSlot.SlotStatus.CONFIRMED, true, true);
        @Override public PickupSlot save(PickupSlot value) { return value; }
        @Override public Optional<PickupSlot> findById(Long id) { return Optional.of(slot); }
        @Override public Optional<PickupSlot> findLatestByOrderId(Long id) { return Optional.of(slot); }
    }

    private static final class FakeEscrows implements EscrowAccountRepositoryPort {
        private final EscrowAccount escrow;
        private FakeEscrows() {
            escrow = new EscrowAccount(11L, BigDecimal.TEN);
            escrow.registerExternalOperationId("paid-operation");
        }
        @Override public EscrowAccount save(EscrowAccount value) { return value; }
        @Override public Optional<EscrowAccount> findByOrderId(Long id) { return Optional.of(escrow); }
        @Override public List<EscrowAccount> findPendingReleaseAccounts(Instant at) { return List.of(); }
        @Override public List<EscrowAccount> findByStatus(EscrowAccount.EscrowStatus status) { return List.of(); }
    }

    private static final class FakeHasher implements PickupPinHashPort {
        @Override public String hash(String rawPin) { return "fake-bcrypt:" + rawPin; }
        @Override public boolean matches(String rawPin, String encodedPin) { return encodedPin != null && encodedPin.equals(hash(rawPin)); }
    }
}
