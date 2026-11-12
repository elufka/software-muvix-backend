package com.muvix.application.usecase.catalogue;

import com.muvix.application.port.in.catalogue.CreateBatchUseCase;
import com.muvix.application.port.out.persistence.CatalogProjectionPort;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.domain.model.catalogue.Batch;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.domain.model.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreateBatchService implements CreateBatchUseCase {

    private final SellableItemRepositoryPort itemRepositoryPort;
    private final CatalogProjectionPort catalogProjectionPort;

    public CreateBatchService(SellableItemRepositoryPort itemRepositoryPort,
                              CatalogProjectionPort catalogProjectionPort) {
        this.itemRepositoryPort = itemRepositoryPort;
        this.catalogProjectionPort = catalogProjectionPort;
    }

    @Override
    @Transactional
    public Batch createFromScratch(CreateBatchFromScratchCommand command) {
        if (command.productsToCreate() == null || command.productsToCreate().size() < 2) {
            throw new DomainException("Un lote requiere al menos 2 productos.");
        }

        // 1. Crear y guardar en MySQL cada producto (público o privado con precio 0)
        List<Product> savedProducts = new ArrayList<>();
        for (NewProductData pData : command.productsToCreate()) {
            Product product = Product.create(
                    command.sellerId(), pData.title(), pData.description(),
                    pData.price(), pData.category(), pData.images()
            );
            Product saved = (Product) itemRepositoryPort.save(product);
            savedProducts.add(saved);

            // Si el producto individual es público, se proyecta también de forma aislada en Cassandra
            if (saved.isPublic()) {
                catalogProjectionPort.projectProductToCatalog(saved, command.zone(), null);
            }
        }

        // 2. Armar la entidad Batch con los productos guardados con ID
        Batch batch = Batch.create(
                command.sellerId(), command.batchTitle(), command.batchDescription(),
                savedProducts, command.batchPrice()
        );

        Batch savedBatch = (Batch) itemRepositoryPort.save(batch);

        // 3. Proyectar el Lote completo en Cassandra
        if (savedBatch.isPublic()) {
            catalogProjectionPort.projectBatchToCatalog(savedBatch, command.zone());
        }

        return savedBatch;
    }

    @Override
    @Transactional
    public Batch createFromExisting(CreateBatchFromExistingCommand command) {
        // 1. Obtener los productos guardados previamente desde MySQL
        List<Product> existingProducts = itemRepositoryPort.findProductsByIds(command.existingProductIds());

        // 2. Validar pertenencia al mismo vendedor y disponibilidad
        Batch batch = Batch.create(
                command.sellerId(), command.batchTitle(), command.batchDescription(),
                existingProducts, command.batchPrice()
        );

        Batch savedBatch = (Batch) itemRepositoryPort.save(batch);

        // 3. Proyectar el Lote en Cassandra
        if (savedBatch.isPublic()) {
            catalogProjectionPort.projectBatchToCatalog(savedBatch, command.zone());
        }

        return savedBatch;
    }
}
