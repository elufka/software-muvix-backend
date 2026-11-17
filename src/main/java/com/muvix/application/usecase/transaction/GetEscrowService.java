package com.muvix.application.usecase.transaction;
import com.muvix.application.port.in.transaction.GetEscrowUseCase;
import com.muvix.application.port.out.persistence.*;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.EscrowAccount;
import org.springframework.stereotype.Service;
import java.util.Objects;
@Service
public class GetEscrowService implements GetEscrowUseCase {
    private final EscrowAccountRepositoryPort escrows; private final OrderRepositoryPort orders; private final SellableItemRepositoryPort items;
    public GetEscrowService(EscrowAccountRepositoryPort escrows,OrderRepositoryPort orders,SellableItemRepositoryPort items){this.escrows=escrows;this.orders=orders;this.items=items;}
    @Override public EscrowAccount getForOrder(Long orderId,Long actorId,boolean moderator){
        var order=orders.findById(orderId).orElseThrow(()->new DomainException("Order not found"));
        boolean participant=Objects.equals(order.getBuyerId(),actorId)||order.getItemIds().stream().map(items::findById).flatMap(java.util.Optional::stream).anyMatch(item->Objects.equals(item.getSellerId(),actorId));
        if(!moderator&&!participant)throw new DomainException("Only order participants or a moderator can read escrow");
        return escrows.findByOrderId(orderId).orElseThrow(()->new DomainException("Escrow not found"));
    }
}
