package com.muvix.application.usecase.catalogue;

import com.muvix.application.port.in.catalogue.CreateProductUseCase;
import com.muvix.application.port.out.persistence.CatalogProjectionPort;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.domain.model.catalogue.SellableItem.State;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProductService implements CreateProductUseCase {

    private final SellableItemRepositoryPort itemRepositoryPort;
    private final CatalogProjectionPort catalogProjectionPort;

    public CreateProductService(SellableItemRepositoryPort itemRepositoryPort,
                                CatalogProjectionPort catalogProjectionPort) {
        this.itemRepositoryPort = itemRepositoryPort;
        this.catalogProjectionPort = catalogProjectionPort;
    }

    @Override
    @Transactional
    public Product createProduct(CreateProductCommand command) {
        Product product = Product.create(
                command.sellerId(),
                command.title(),
                command.description(),
                command.price(),
                command.category(),
                command.condition(),
                command.images()
        );


        Product savedProduct = (Product) itemRepositoryPort.save(product);

        if (savedProduct.isPublic()) {
            catalogProjectionPort.projectProductToCatalog(savedProduct, command.boxId());
        }

        return savedProduct;
    }
}