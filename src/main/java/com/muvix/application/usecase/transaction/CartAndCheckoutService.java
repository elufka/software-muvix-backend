package com.muvix.application.usecase.transaction;

import com.muvix.application.port.in.transaction.CartAndCheckoutUseCase;
import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.application.port.out.persistence.RelocationEventRepositoryPort;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.management.PurchaseEvent;
import com.muvix.domain.model.management.RelocationEvent;
import com.muvix.domain.model.management.SaleEvent;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.domain.model.transaction.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class CartAndCheckoutService implements CartAndCheckoutUseCase {

    private final SellableItemRepositoryPort itemRepository;
    private final RelocationEventRepositoryPort eventRepository;
    private final OrderRepositoryPort orderRepository;
    private final EscrowAccountRepositoryPort escrowRepository;
    private final MobbexGatewayPort mobbexGateway;

    public CartAndCheckoutService(SellableItemRepositoryPort itemRepository,
                                  RelocationEventRepositoryPort eventRepository,
                                  OrderRepositoryPort orderRepository,
                                  EscrowAccountRepositoryPort escrowRepository,
                                  MobbexGatewayPort mobbexGateway) {
        this.itemRepository = itemRepository;
        this.eventRepository = eventRepository;
        this.orderRepository = orderRepository;
        this.escrowRepository = escrowRepository;
        this.mobbexGateway = mobbexGateway;
    }

    @Override
    public void addItemToCart(AddToCartCommand command) {
        SellableItem item = itemRepository.findById(command.itemId())
                .orElseThrow(() -> new DomainException("El ítem no existe"));

        if (!item.canBePurchased()) {
            throw new DomainException("El ítem no está disponible para compra");
        }
        if (item.getSellerId().equals(command.buyerId())) {
            throw new DomainException("No puedes agregar tus propios productos al carrito");
        }

        // Lógica de persistencia en sesión/Redis del Carrito
    }

    @Override
    @Transactional
    public Order checkout(CheckoutCartCommand command) {
        // 1. Obtener la Box del comprador asociada a la compra
        RelocationEvent buyerEvent = eventRepository.findById(command.targetEventId())
                .orElseThrow(() -> new DomainException("Box no encontrada"));

        // 2. Obtener ítems del carrito
        List<SellableItem> itemsToBuy = itemRepository.findCartItemsByBuyerId(command.buyerId());
        if (itemsToBuy.isEmpty()) {
            throw new DomainException("El carrito está vacío");
        }

        // Calcular total
        BigDecimal totalAmount = itemsToBuy.stream()
                .map(SellableItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Set<Long> itemIds = Set.copyOf(itemsToBuy.stream().map(SellableItem::getId).toList());

        // 3. Crear el Pedido (Order)
        Order order = new Order(
                null,
                buyerEvent.getId(),
                command.buyerId(),
                itemIds,
                totalAmount,
                Order.Origin.DIRECT_PURCHASE,
                command.deliveryMethod(),
                null,
                Order.Status.PENDING_SLOT
        );

        // ===== REGLA CONTEXTUAL DE LOGÍSTICA =====
        // Si el usuario SOLO tiene una Box de Venta (o ninguna Box de Compra), no se habilita la logística de fechas de ingreso
        if (buyerEvent instanceof SaleEvent) {
            // El pedido se completa directamente como retiro puntual sin rango de fechas de llegada
        }

        Order savedOrder = orderRepository.save(order);

        // 4. Iniciar custodia de fondos (Escrow en Mobbex)
        EscrowAccount escrowAccount = new EscrowAccount(savedOrder.getId(), savedOrder.getTotal());
        escrowRepository.save(escrowAccount);

        // 5. Marcar productos como reservados
        itemsToBuy.forEach(item -> {
            item.reserve();
            itemRepository.save(item);
        });

        return savedOrder;
    }
}