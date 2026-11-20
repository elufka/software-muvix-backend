package com.muvix.application.port.in.catalogue;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.ProductCondition;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
public interface ManageSellableItemUseCase {
    record ReplaceCommand(Long itemId, Long sellerId, String title, String description, BigDecimal price,
                          String category, ProductCondition condition, List<String> images, Set<Long> productIds) {}
    SellableItem get(Long itemId, Long sellerId);
    SellableItem replace(ReplaceCommand command);
    void delete(Long itemId, Long sellerId);
}
