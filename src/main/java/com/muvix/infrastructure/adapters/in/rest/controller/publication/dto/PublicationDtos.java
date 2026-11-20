package com.muvix.infrastructure.adapters.in.rest.controller.publication.dto;


import com.muvix.application.port.in.catalogue.CreateBatchUseCase.NewProductData;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.ProductCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public sealed interface PublicationDtos {

    // --- REQUESTS ---

    record CreateProductRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotNull @PositiveOrZero BigDecimal price,
            @NotBlank String category,
            @NotNull ProductCondition condition,
            @NotBlank String zone,
            List<String> images,
            Long boxId
    ) implements PublicationDtos {}

    record CreateBatchRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotNull @PositiveOrZero BigDecimal price,
            @NotBlank String category,
            @NotBlank String zone,
            List<NewProductData> productsToCreate,
            Set<Long> existingProductIds
    ) implements PublicationDtos {}

    record ReplaceItemRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotNull @PositiveOrZero BigDecimal price,
            String category,
            ProductCondition condition,
            List<String> images,
            Set<Long> productIds
    ) implements PublicationDtos {}

    // --- RESPONSES ---

    record PublicationResponse(
            Long id,
            String type,          // "PRODUCT" o "BATCH"
            Long sellerId,
            String title,
            String description,
            BigDecimal price,
            String state,
            String category,
            ProductCondition condition,// Solo en Product (null en Batch)
            List<String> images,  // Solo en Product (lista vacía en Batch)
            Set<Long> productIds  // Solo en Batch (conjunto vacío en Product)
    ) implements PublicationDtos {}

    record CatalogPageResponse(
            List<PublicationResponse> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) implements PublicationDtos {}
}