package com.muvix.application.port.in.catalogue;

import com.muvix.domain.model.catalogue.Product;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.ProductCondition;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;

public interface CreateStorageItemUseCase {
    record CreateStoragareItemCommand(
            Long sellerId,
            String title,
            String description,// 0 = Privado, >0 = Público
            String category,
            ProductCondition condition,
            List<String> images
    ) {}

    Product createStorageItem(CreateStorageItemUseCase.CreateStoragareItemCommand command);
}
