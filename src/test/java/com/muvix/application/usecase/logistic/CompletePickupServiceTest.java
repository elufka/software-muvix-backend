package com.muvix.application.usecase.logistic;

import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.application.port.out.persistence.PickupSlotRepositoryPort;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.application.port.out.security.PickupPinHashPort;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.logistic.AuthorizedPickupPerson;
import com.muvix.domain.model.logistic.PickupSlot;
import com.muvix.domain.model.transaction.Order;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.ProductCondition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CompletePickupServiceTest {
    @Test
    void sellerCanCompleteConfirmedPickupWithValidPin() {
        Fixture fixture = new Fixture();

        fixture.service.complete(11L, 8L, fixture.pin);

        assertEquals(Order.Status.DELIVERED, fixture.order.getStatus());
        assertNotNull(fixture.order.getDeliveredAt());
        assertEquals(1, fixture.orders.saveCount);
    }

    @Test
    void invalidPinDoesNotCompletePickup() {
        Fixture fixture = new Fixture();

        assertThrows(DomainException.class, () -> fixture.service.complete(11L, 8L, "000000"));

        assertEquals(Order.Status.SLOT_CONFIRMED, fixture.order.getStatus());
        assertEquals(0, fixture.orders.saveCount);
    }

    @Test
    void buyerCannotSubmitSellerPickupVerification() {
        Fixture fixture = new Fixture();

        assertThrows(DomainException.class, () -> fixture.service.complete(11L, 7L, fixture.pin));

        assertEquals(Order.Status.SLOT_CONFIRMED, fixture.order.getStatus());
        assertEquals(0, fixture.orders.saveCount);
    }

    @Test
    void pickupCannotCompleteBeforePaymentIsHeld() {
        Fixture fixture = new Fixture(false);

        assertThrows(DomainException.class, () -> fixture.service.complete(11L, 8L, fixture.pin));

        assertEquals(Order.Status.SLOT_CONFIRMED, fixture.order.getStatus());
        assertEquals(0, fixture.orders.saveCount);
    }

    private static final class Fixture {
        private final Order order;
        private final String pin = "123456";
        private final FakeOrders orders;
        private final CompletePickupService service;

        private Fixture() { this(true); }

        private Fixture(boolean paid) {
            order = new Order(11L, null, 7L, "Buyer", "Seller", Set.of(55L), BigDecimal.TEN,
                    Order.Origin.DIRECT_PURCHASE, Order.DeliveryMethod.PICKUP, null, Order.Status.PENDING_SLOT);
            order.setAuthorizedPickupPerson(new AuthorizedPickupPerson("Buyer", "DOC-1", "555-0100"));
            order.confirmSlot(22L);
            order.assignPickupPinHash("hash:" + pin);
            orders = new FakeOrders(order);
            PickupSlot slot = PickupSlot.restore(22L, 11L, Instant.parse("2026-11-23T10:00:00Z"),
                    Instant.parse("2026-11-23T11:00:00Z"), PickupSlot.SlotStatus.CONFIRMED, true, true);
            service = new CompletePickupService(orders, new FakeSlots(slot), new FakeItems(), new FakeEscrows(paid), new FakePinHasher());
        }
    }

    private static final class FakeOrders implements OrderRepositoryPort {
        private final Order order;
        private int saveCount;
        private FakeOrders(Order order) { this.order = order; }
        @Override public Order save(Order value) { saveCount++; return value; }
        @Override public Optional<Order> findById(Long id) { return Optional.of(order); }
        @Override public List<Order> findDeliveredBefore(Instant deliveredBefore) { return List.of(); }
    }

    private static final class FakeSlots implements PickupSlotRepositoryPort {
        private final PickupSlot slot;
        private FakeSlots(PickupSlot slot) { this.slot = slot; }
        @Override public PickupSlot save(PickupSlot value) { return value; }
        @Override public Optional<PickupSlot> findById(Long id) { return slot.getId().equals(id) ? Optional.of(slot) : Optional.empty(); }
        @Override public Optional<PickupSlot> findLatestByOrderId(Long orderId) { return Optional.of(slot); }
    }

    private static final class FakeItems implements SellableItemRepositoryPort {
        private final Product product = Product.reconstitute(55L, 8L, "Item", "", BigDecimal.TEN,
                SellableItem.State.SOLD, "Other", ProductCondition.GOOD, List.of("image.jpg"));
        @Override public SellableItem save(SellableItem item) { return item; }
        @Override public Optional<SellableItem> findById(Long id) { return id == 55L ? Optional.of(product) : Optional.empty(); }
        @Override public List<Product> findProductsByIds(Set<Long> productIds) { return List.of(product); }
        @Override public List<SellableItem> findBySellerId(Long sellerId) { return List.of(product); }
        @Override public com.muvix.application.port.in.catalogue.SellableItemQueryUseCase.CatalogPage searchPublicCatalog(
                com.muvix.application.port.in.catalogue.SellableItemQueryUseCase.CatalogFilterQuery query) {
            return null;
        }
    }

    private static final class FakeEscrows implements EscrowAccountRepositoryPort {
        private final EscrowAccount escrow;
        private FakeEscrows(boolean paid) {
            escrow = new EscrowAccount(11L, BigDecimal.TEN);
            if (paid) escrow.registerExternalOperationId("paid-operation");
        }
        @Override public EscrowAccount save(EscrowAccount value) { return value; }
        @Override public Optional<EscrowAccount> findByOrderId(Long orderId) { return Optional.of(escrow); }
        @Override public List<EscrowAccount> findPendingReleaseAccounts(Instant at) { return List.of(); }
        @Override public List<EscrowAccount> findByStatus(EscrowAccount.EscrowStatus status) { return List.of(); }
    }

    private static final class FakePinHasher implements PickupPinHashPort {
        @Override public String hash(String rawPin) { return "hash:" + rawPin; }
        @Override public boolean matches(String rawPin, String encodedPin) {
            return encodedPin != null && encodedPin.equals(hash(rawPin));
        }
    }
}
