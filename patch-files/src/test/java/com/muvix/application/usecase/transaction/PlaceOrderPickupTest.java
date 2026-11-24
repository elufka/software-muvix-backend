package com.muvix.application.usecase.transaction;

import com.muvix.application.port.in.transaction.PlaceOrderUseCase;
import com.muvix.application.port.in.catalogue.SellableItemQueryUseCase;
import com.muvix.application.port.out.event.EventPublisherPort;
import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.application.port.out.persistence.*;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.event.DomainEvent;
import com.muvix.domain.model.event.OrderConfirmedEvent;
import com.muvix.domain.model.logistic.AuthorizedPickupPerson;
import com.muvix.domain.model.logistic.PickupSlot;
import com.muvix.domain.model.management.Box;
import com.muvix.domain.model.negotiation.Offer;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.domain.model.transaction.Order;
import com.muvix.domain.model.user.User;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.ProductCondition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PlaceOrderPickupTest {
    @Test
    void directCheckoutCanCreateLinkedPickupProposalAndNamedHistoryEvent() {
        Fixture fixture = new Fixture();
        PlaceOrderUseCase service = new PlaceOrderService(fixture.items, fixture.events, fixture.orders,
                fixture.escrows, fixture.mobbex, fixture.offers, fixture.publisher, fixture.users, fixture.slots);

        PlaceOrderUseCase.CheckoutResult result = service.executeDirectPurchase(
                new PlaceOrderUseCase.DirectPurchaseCommand(7L, null, Set.of(55L),
                        new PlaceOrderUseCase.PickupSlotProposal(Instant.parse("2026-12-01T10:00:00Z"),
                                Instant.parse("2026-12-01T11:00:00Z"),
                                new AuthorizedPickupPerson("Buyer", "DOC-1", "555-0100"))));

        assertEquals("Buyer", result.order().getBuyerName());
        assertEquals("Seller", result.order().getSellerName());
        assertEquals(201L, result.order().getSlotId());
        assertNull(result.order().getPickupPinHash());
        assertEquals(100L, fixture.proposedSlot.getOrderId());
        OrderConfirmedEvent event = fixture.publisher.published.stream()
                .filter(OrderConfirmedEvent.class::isInstance).map(OrderConfirmedEvent.class::cast)
                .findFirst().orElseThrow();
        assertEquals("Buyer", event.getBuyerName());
        assertEquals("Seller", event.getSellerName());
        assertEquals(1, fixture.escrows.saved.size());
    }

    private static final class Fixture {
        private final Product product = Product.reconstitute(55L, 8L, "Item", "", BigDecimal.TEN,
                SellableItem.State.AVAILABLE, "Other", ProductCondition.GOOD, List.of("image.jpg"));
        private final FakeItems items = new FakeItems(product);
        private final FakeOrders orders = new FakeOrders();
        private final FakeSlots slots = new FakeSlots();
        private final FakeEscrows escrows = new FakeEscrows();
        private final FakePublisher publisher = new FakePublisher();
        private final FakeUsers users = new FakeUsers();
        private final FakeEvents events = new FakeEvents();
        private final FakeOffers offers = new FakeOffers();
        private final MobbexGatewayPort mobbex = new FakeMobbex();
        private PickupSlot proposedSlot;

        private Fixture() { slots.fixture = this; }
    }

    private static final class FakeItems implements SellableItemRepositoryPort {
        private final Product product;
        private FakeItems(Product product) { this.product = product; }
        @Override public SellableItem save(SellableItem item) { return item; }
        @Override public Optional<SellableItem> findById(Long id) { return id.equals(product.getId()) ? Optional.of(product) : Optional.empty(); }
        @Override public List<Product> findProductsByIds(Set<Long> ids) { return List.of(product); }
        @Override public List<SellableItem> findBySellerId(Long sellerId) { return List.of(product); }
        @Override public SellableItemQueryUseCase.CatalogPage searchPublicCatalog(SellableItemQueryUseCase.CatalogFilterQuery query) { return null; }
    }

    private static final class FakeOrders implements OrderRepositoryPort {
        @Override public Order save(Order value) {
            if (value.getId() != null) return value;
            return Order.restore(100L, value.getEventId(), value.getBuyerId(), value.getBuyerName(), value.getSellerName(),
                    value.getItemIds(), value.getSourceBatchIds(), value.getTotal(), value.getOrigin(),
                    value.getDeliveryMethod(), value.getSlotId(), value.getStatus(), value.getAuthorizedPickupPerson(),
                    value.getPickupPinHash(), value.getDeliveredAt());
        }
        @Override public Optional<Order> findById(Long id) { return Optional.empty(); }
        @Override public List<Order> findDeliveredBefore(Instant at) { return List.of(); }
        @Override public List<Order> findPendingSlotBefore(Instant createdBefore) { return List.of(); }
    }

    private static final class FakeSlots implements PickupSlotRepositoryPort {
        private Fixture fixture;
        @Override public PickupSlot save(PickupSlot slot) {
            PickupSlot saved = PickupSlot.restore(201L, slot.getOrderId(), slot.getProposedStartTime(),
                    slot.getProposedEndTime(), slot.getStatus(), slot.isBuyerConfirmed(), slot.isSellerConfirmed());
            fixture.proposedSlot = saved;
            return saved;
        }
        @Override public Optional<PickupSlot> findById(Long id) { return Optional.empty(); }
        @Override public Optional<PickupSlot> findLatestByOrderId(Long id) { return Optional.empty(); }
    }

    private static final class FakeEscrows implements EscrowAccountRepositoryPort {
        private final List<EscrowAccount> saved = new ArrayList<>();
        @Override public EscrowAccount save(EscrowAccount account) { saved.add(account); return account; }
        @Override public Optional<EscrowAccount> findByOrderId(Long id) { return Optional.empty(); }
        @Override public List<EscrowAccount> findPendingReleaseAccounts(Instant at) { return List.of(); }
        @Override public List<EscrowAccount> findByStatus(EscrowAccount.EscrowStatus status) { return List.of(); }
    }

    private static final class FakePublisher implements EventPublisherPort {
        private final List<DomainEvent> published = new ArrayList<>();
        @Override public void publish(DomainEvent event) { published.add(event); }
        @Override public void publishAll(List<DomainEvent> events) { published.addAll(events); }
    }

    private static final class FakeUsers implements UserRepositoryPort {
        @Override public User save(User user) { return user; }
        @Override public Optional<User> findById(Long id) {
            return Optional.of(id == 7L ? new User(7L, "Buyer", "buyer@test", "555")
                    : new User(8L, "Seller", "seller@test", "555"));
        }
        @Override public Optional<User> findByEmail(String email) { return Optional.empty(); }
        @Override public boolean existsByEmail(String email) { return false; }
        @Override public void updateMobbexConnectDetails(Long id, String connectId, boolean linked) {}
    }

    private static final class FakeEvents implements RelocationEventRepositoryPort {
        @Override public Box save(Box box) { return box; }
        @Override public Optional<Box> findById(Long id) { return Optional.empty(); }
        @Override public Optional<Box> findByShareToken(String token) { return Optional.empty(); }
        @Override public List<Box> findByUserId(Long id) { return List.of(); }
        @Override public List<Long> findItemIdsByEventId(Long id) { return List.of(); }
        @Override public void linkItemToBox(Long boxId, Long itemId) {}
        @Override public void unlinkItemFromBox(Long boxId, Long itemId) {}
    }

    private static final class FakeOffers implements OfferRepositoryPort {
        @Override public Offer save(Offer offer) { return offer; }
        @Override public Optional<Offer> findById(Long id) { return Optional.empty(); }
    }

    private static final class FakeMobbex implements MobbexGatewayPort {
        @Override public CheckoutResponse createCheckout(CheckoutRequest request) { return new CheckoutResponse("https://checkout.test", "payment-1"); }
        @Override public boolean isApprovedOperation(String reference, String operationId, BigDecimal amount) { return false; }
        @Override public void releaseFunds(String id) {}
        @Override public void refundPayment(String id) {}
        @Override public String generateConnectUrl(String reference, String returnUrl) { return ""; }
    }
}
