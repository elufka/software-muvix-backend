package com.muvix.application.usecase.catalogue;

import com.muvix.application.port.in.catalogue.CreateProductUseCase;
import com.muvix.application.port.out.persistence.CatalogProjectionPort;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.domain.model.catalogue.SellableItem.State;
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
        // 1. Crear entidad de Dominio
        Product product = Product.create(
                command.sellerId(),
                command.title(),
                command.description(),
                command.price(),
                command.category(),
                command.images()
        );

        // 2. Persistir en MySQL (Transactional Write Model)
        Product savedProduct = (Product) itemRepositoryPort.save(product);

        // 3. Proyectar en Cassandra (Read Model para Homepage y Búsquedas) si es público
        if (savedProduct.isPublic()) {
            catalogProjectionPort.projectProductToCatalog(savedProduct, command.zone(), command.boxId());
        }

        return savedProduct;
    }
}