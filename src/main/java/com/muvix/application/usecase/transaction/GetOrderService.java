package com.muvix.application.usecase.transaction;
import com.muvix.application.port.in.transaction.GetOrderUseCase;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.Order;
import org.springframework.stereotype.Service;
import java.util.Objects;
@Service
public class GetOrderService implements GetOrderUseCase {
    private final OrderRepositoryPort orders; private final SellableItemRepositoryPort items;
    public GetOrderService(OrderRepositoryPort orders,SellableItemRepositoryPort items){this.orders=orders;this.items=items;}
    @Override public Order get(Long orderId,Long actorId){
        Order order=orders.findById(orderId).orElseThrow(()->new DomainException("Order not found"));
        boolean buyer=Objects.equals(order.getBuyerId(),actorId);
        boolean seller=order.getItemIds().stream().map(items::findById).flatMap(java.util.Optional::stream)
                .anyMatch(item->Objects.equals(item.getSellerId(),actorId));
        if(!buyer&&!seller)throw new DomainException("Only an order participant can read it");
        return order;
    }
}
