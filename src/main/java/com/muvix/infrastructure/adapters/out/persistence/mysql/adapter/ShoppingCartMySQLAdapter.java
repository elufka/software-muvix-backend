package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;
import com.muvix.application.port.out.persistence.ShoppingCartRepositoryPort;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.ShoppingCartJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.ShoppingCartJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Component @Transactional
public class ShoppingCartMySQLAdapter implements ShoppingCartRepositoryPort {
    private final ShoppingCartJpaRepository repository;
    public ShoppingCartMySQLAdapter(ShoppingCartJpaRepository repository){this.repository=repository;}
    @Override @Transactional(readOnly=true) public List<Long> findItemIds(Long buyerId){return repository.findById(buyerId).map(c->List.copyOf(c.getItemIds())).orElse(List.of());}
    @Override public void addItem(Long buyerId,Long itemId){
        ShoppingCartJpaEntity cart=repository.findById(buyerId).orElseGet(()->{ShoppingCartJpaEntity c=new ShoppingCartJpaEntity();c.setBuyerId(buyerId);return c;});
        cart.getItemIds().add(itemId);repository.save(cart);
    }
    @Override public void removeItem(Long buyerId,Long itemId){
        repository.findById(buyerId).ifPresent(cart->{cart.getItemIds().remove(itemId);if(cart.getItemIds().isEmpty())repository.delete(cart);});
    }
    @Override public void clear(Long buyerId){repository.deleteById(buyerId);}
}
