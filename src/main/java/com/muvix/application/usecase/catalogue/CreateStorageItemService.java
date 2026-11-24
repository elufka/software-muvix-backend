package com.muvix.application.usecase.catalogue;

import com.muvix.application.port.in.catalogue.CreateStorageItemUseCase;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.domain.model.catalogue.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CreateStorageItemService implements CreateStorageItemUseCase {
    private final SellableItemRepositoryPort itemRepositoryPort;

    @Override
    public Product createStorageItem(CreateStoragareItemCommand command) {
        Product product = Product.createStorageItem(
                command.sellerId(),
                command.title(),
                command.description(),
                command.category(),
                command.condition(),
                command.images()
        );

        Product savedProduct = (Product) itemRepositoryPort.save(product);

        return savedProduct;
    }
}
