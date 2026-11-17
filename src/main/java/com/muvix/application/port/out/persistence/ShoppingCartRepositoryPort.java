package com.muvix.application.port.out.persistence;
import java.util.List;
public interface ShoppingCartRepositoryPort {
    List<Long> findItemIds(Long buyerId);
    void addItem(Long buyerId, Long itemId);
    void removeItem(Long buyerId, Long itemId);
    void clear(Long buyerId);
}
