package com.muvix.application.usecase.transaction;

import com.muvix.application.port.in.transaction.PlaceOrderUseCase;
import com.muvix.application.port.in.logistic.ChatCoordinatorUseCase;
import com.muvix.application.port.out.event.EventPublisherPort;
import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.application.port.out.persistence.OfferRepositoryPort;
import com.muvix.application.port.out.persistence.BoxRepositoryPort;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.application.port.out.persistence.UserRepositoryPort;
import com.muvix.application.port.out.persistence.PickupSlotRepositoryPort;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.catalogue.Batch;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.event.OrderConfirmedEvent;
import com.muvix.domain.model.user.User;
import com.muvix.domain.model.management.Box;
import com.muvix.domain.model.management.SaleBox;
import com.muvix.domain.model.negotiation.Offer;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.domain.model.transaction.Order;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class PlaceOrderService implements PlaceOrderUseCase {
    private final SellableItemRepositoryPort items;
    private final BoxRepositoryPort boxRepositoryPort;
    private final OrderRepositoryPort orders;
    private final EscrowAccountRepositoryPort escrows;
    private final MobbexGatewayPort mobbex;
    private final OfferRepositoryPort offers;
    private final EventPublisherPort eventPublisher;
    private final UserRepositoryPort users;
    private final PickupSlotRepositoryPort pickupSlots;
    private final ChatCoordinatorUseCase chatCoordinator;

    @Autowired
    public PlaceOrderService(SellableItemRepositoryPort items,
                             BoxRepositoryPort boxRepositoryPort,
                             OrderRepositoryPort orders,
                             EscrowAccountRepositoryPort escrows,
                             MobbexGatewayPort mobbex,
                             OfferRepositoryPort offers,
                             EventPublisherPort eventPublisher,
                             UserRepositoryPort users,
                             PickupSlotRepositoryPort pickupSlots,
                             ChatCoordinatorUseCase chatCoordinator) {
        this.items = items;
        this.boxRepositoryPort = boxRepositoryPort;
        this.orders = orders;
        this.escrows = escrows;
        this.mobbex = mobbex;
        this.offers = offers;
        this.eventPublisher = eventPublisher;
        this.users = users;
        this.pickupSlots = pickupSlots;
        this.chatCoordinator=chatCoordinator;
    }

    public PlaceOrderService(SellableItemRepositoryPort items,
                             BoxRepositoryPort boxRepositoryPort,
                             OrderRepositoryPort orders,
                             EscrowAccountRepositoryPort escrows,
                             MobbexGatewayPort mobbex,
                             OfferRepositoryPort offers,
                             EventPublisherPort eventPublisher,
                             UserRepositoryPort users,
                             PickupSlotRepositoryPort pickupSlots) {

        this(items, boxRepositoryPort,orders,escrows,mobbex,offers,eventPublisher,users,pickupSlots,null);
    }

    @Override
    @Transactional
    public CheckoutResult executeDirectPurchase(DirectPurchaseCommand command) {
        if (command == null ||
                command.buyerId() == null ||
                command.itemIds() == null ||
                command.itemIds().isEmpty())
            throw new DomainException("Buyer and at least one item are required");
        if (command.eventId() != null) {
            Box box = boxRepositoryPort.findById(command.eventId())
                    .orElseThrow(() -> new DomainException("Purchase box not found"));

            if (!(box instanceof com.muvix.domain.model.management.PurchaseBox) || !box.getUserId().equals(command.buyerId()))
                throw new DomainException("The optional box must be the buyer's purchase box");
        }

        List<SellableItem> selected = command.itemIds().stream().map(id -> items.findById(id)
                .orElseThrow(() -> new DomainException("Sellable item not found: " + id))).toList();

        return checkout(command.buyerId(),
                command.eventId(),
                selected,
                Order.Origin.DIRECT_PURCHASE,
                null,
                "Muvix direct purchase",
                command.pickupSlot());
    }

    @Override
    @Transactional
    public CheckoutResult executeTakeItAllPurchase(TakeItAllPurchaseCommand command) {
        if (command == null || command.buyerId() == null || command.eventId() == null)
            throw new DomainException("Buyer and sale box are required");

        Box box = boxRepositoryPort.findById(command.eventId())
                .orElseThrow(() -> new DomainException("Sale box not found"));

        if (!(box instanceof SaleBox sale) || !sale.isActive())
            throw new DomainException("Take It All requires an active sale box");

        if (sale.getUserId().equals(command.buyerId())) throw new DomainException("You cannot buy your own sale box");

        List<Long> ids = boxRepositoryPort.findItemIdsByEventId(sale.getId());
        if (ids.isEmpty()) throw new DomainException("The sale box has no items");

        List<SellableItem> available = ids.stream().map(id -> items.findById(id).orElse(null))
                .filter(item -> item != null && item.canBePurchased()).toList();

        if (available.isEmpty()) throw new DomainException("The sale box has no currently available items");

        return checkout(command.buyerId(), sale.getId(), available, Order.Origin.TAKE_IT_ALL,
                sale.getTakeItAllPrice(), "Muvix Take It All: " + sale.getTitle(), command.pickupSlot());
    }

    @Override
    @Transactional
    public CheckoutResult executeOfferPurchase(AcceptOfferPurchaseCommand command) {
        Offer offer = offers.findById(command.offerId()).orElseThrow(() -> new DomainException("Offer not found"));
        SellableItem item = items.findById(offer.getItemId()).orElseThrow(() -> new DomainException("Sellable item not found"));

        if(offer.getStatus()==Offer.Status.PENDING){
            if(!item.getSellerId().equals(command.actorId()))throw new DomainException("Only the seller can accept or counter an offer");
            offer.accept();
        } else if (offer.getStatus()==Offer.Status.COUNTERED){
            offer.acceptCounter(command.actorId());
        } else throw new DomainException("Offer has already been resolved");

        offers.save(offer);
        eventPublisher.publishAll(offer.getDomainEvents());

        CheckoutResult result=checkout(offer.getBuyerId(), null, List.of(item), Order.Origin.ACCEPTED_OFFER,
                offer.getAgreedAmount(), "Muvix accepted offer " + offer.getId(), command.pickupSlot());

        if(chatCoordinator!=null)chatCoordinator.linkOfferMessagesToOrder(offer.getId(),result.order().getId());

        return result;
    }

    private CheckoutResult checkout(Long buyerId, Long eventId, List<SellableItem> selected,
                                    Order.Origin origin, BigDecimal fixedPrice, String description,
                                    PickupSlotProposal pickupProposal) {
        if(pickupProposal!=null)throw new DomainException("Pickup time and authorized person are now coordinated in the order chat");
        if (selected.isEmpty()) throw new DomainException("Order must contain at least one available item");

        Long seller = selected.get(0).getSellerId();

        if (selected.stream().anyMatch(item -> !seller.equals(item.getSellerId())))
            throw new DomainException("An order can contain items from only one seller");
        if (selected.stream().anyMatch(item -> !item.canBePurchased() || buyerId.equals(item.getSellerId())))
            throw new DomainException("Items must be available and belong to another seller");

        Set<Long> coveredProducts = new java.util.HashSet<>();
        for (SellableItem item : selected) {
            if (item instanceof Batch batch) {
                for (Long childId : batch.getProductIds()) {
                    if (!coveredProducts.add(childId)) throw new DomainException("Order contains overlapping batch contents");
                }
            } else if (!coveredProducts.add(item.getId())) {
                throw new DomainException("Order contains an item both individually and through a batch");
            }
        }

        BigDecimal total = fixedPrice == null ? selected.stream().map(SellableItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add) : fixedPrice;

        Set<Long> selectedProductIds = selected.stream().filter(Product.class::isInstance)
                .map(SellableItem::getId).collect(java.util.stream.Collectors.toSet());

        Set<Long> sourceBatchIds = selectedProductIds.isEmpty() ? Set.of() : items.findBySellerId(seller).stream()
                .filter(Batch.class::isInstance).map(Batch.class::cast)
                .filter(batch -> batch.getState() == SellableItem.State.AVAILABLE
                                 && selectedProductIds.stream().anyMatch(batch::contains))
                .map(Batch::getId)
                .collect(java.util.stream.Collectors.toSet());

        User buyer = users.findById(buyerId).orElseThrow(() -> new DomainException("Buyer not found"));

        User sellerUser = users.findById(seller).orElseThrow(() -> new DomainException("Seller not found"));

        Order pendingOrder = new Order(
                null,
                eventId,
                buyerId,
                buyer.getFirstName() + buyer.getLastName(),
                sellerUser.getFirstName() + sellerUser.getLastName(),
                Set.copyOf(selected.stream().map(SellableItem::getId).toList()), total,
                origin, Order.DeliveryMethod.PICKUP, null, Order.Status.PENDING_SLOT);

        pendingOrder.recordSourceBatchIds(sourceBatchIds);

        Order order = orders.save(pendingOrder);

        if(chatCoordinator!=null)chatCoordinator.initializeOrderChat(order.getId(),false);

        EscrowAccount escrow = new EscrowAccount(order.getId(), total);

        for (SellableItem item : selected) {
            item.reserve();
            items.save(item);
            if (item instanceof Batch batch) {
                for (Long childId : batch.getProductIds()) {
                    SellableItem child = items.findById(childId)
                            .orElseThrow(() -> new DomainException("Batch product not found: " + childId));


                    if (!child.canBePurchased()) throw new DomainException("A batch product is no longer available");

                    child.reserve();
                    items.save(child);
                }
            }
        }
        escrows.save(escrow);

        eventPublisher.publish(new OrderConfirmedEvent(order.getId(), order.getBuyerName(), order.getSellerName(),
                order.getEventId(), order.getTotal()));

        return new CheckoutResult(order, null);
    }
}