package com.muvix.infrastructure.adapters.in.rest.controller.publication.mapper;

import com.muvix.application.port.in.catalogue.CreateBatchUseCase;
import com.muvix.application.port.in.catalogue.CreateProductUseCase;
import com.muvix.application.port.in.catalogue.CreateStorageItemUseCase;
import com.muvix.application.port.in.catalogue.ManageSellableItemUseCase;
import com.muvix.application.port.in.catalogue.SellableItemQueryUseCase.CatalogPage;
import com.muvix.domain.model.catalogue.Batch;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.infrastructure.adapters.in.rest.controller.publication.dto.PublicationDtos.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class PublicationRestMapper {

    public CreateProductUseCase.CreateProductCommand toCommand(Long sellerId, CreateProductRequest r) {
        return new CreateProductUseCase.CreateProductCommand(
                sellerId, r.title(), r.description(), r.price(), r.category(), r.condition() , r.images(), r.boxId()
        );
    }

    public CreateBatchUseCase.CreateBatchFromScratchCommand toScratchCommand(Long sellerId, CreateBatchRequest r) {
        return new CreateBatchUseCase.CreateBatchFromScratchCommand(
                sellerId, r.title(), r.description(), r.price(), r.category(), r.productsToCreate()
        );
    }

    public CreateStorageItemUseCase.CreateStoragareItemCommand toCommand(Long sellerId, CreateStoragareItemRequest r) {
        return new CreateStorageItemUseCase.CreateStoragareItemCommand(
                sellerId, r.title(), r.description(),r.category(), r.condition() , r.images()
        );
    }

    public CreateBatchUseCase.CreateBatchFromExistingCommand toExistingCommand(Long sellerId, CreateBatchRequest r) {
        return new CreateBatchUseCase.CreateBatchFromExistingCommand(
                sellerId, r.title(), r.description(), r.price(), r.category(), r.existingProductIds()
        );
    }

    public ManageSellableItemUseCase.ReplaceCommand toReplaceCommand(Long itemId, Long sellerId, ReplaceItemRequest r) {
        return new ManageSellableItemUseCase.ReplaceCommand(
                itemId, sellerId, r.title(), r.description(), r.price(), r.category(),r.condition(),r.images(), r.productIds()
        );
    }

    public PublicationResponse toResponse(SellableItem item) {
        return switch (item) {
            case Product p -> new PublicationResponse(
                    p.getId(),
                    "INDIVIDUAL",
                    p.getSellerId(),
                    p.getTitle(),
                    p.getDescription(),
                    p.getPrice(),
                    "AVAILABLE",
                    p.getCategory(),
                    p.getCondition(),
                    p.getImages() != null ? p.getImages() : List.of(),
                    Set.of()
            );
            case Batch b -> new PublicationResponse(
                    b.getId(),
                    "BATCH",
                    b.getSellerId(),
                    b.getTitle(),
                    b.getDescription(),
                    b.getPrice(),
                    b.getState().name(),
                    b.getCategory(),
                    null,
                    List.of(),
                    b.getProductIds() != null ? b.getProductIds() : Set.of()
            );
        };
    }

    public CatalogPageResponse toPageResponse(CatalogPage page) {
        List<PublicationResponse> mappedItems = page.items().stream()
                .map(this::toResponse)
                .toList();

        return new CatalogPageResponse(
                mappedItems,
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }
}
