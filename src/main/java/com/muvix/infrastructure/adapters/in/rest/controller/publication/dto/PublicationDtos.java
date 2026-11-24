package com.muvix.infrastructure.adapters.in.rest.controller.publication.dto;


import com.muvix.application.port.in.catalogue.CreateBatchUseCase.NewProductData;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.ProductCondition;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

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
            List<String> images,
            Long boxId
    ) implements PublicationDtos {}

    record CreateBatchRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotNull @PositiveOrZero BigDecimal price,
            @NotBlank String category,
            List<NewProductData> productsToCreate,
            Set<Long> existingProductIds
    ) implements PublicationDtos {}

    record CreateStoragareItemRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotBlank String category,
            @NotNull ProductCondition condition,
            List<String> images
    ) implements PublicationDtos {}

    record ReplaceItemRequest(

            @NotBlank(message = "El título es obligatorio")
            @Size(min = 5, max = 120, message = "El título debe tener entre 5 y 120 caracteres")
            String title,

            @NotBlank(message = "La descripción es obligatoria")
            @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
            String description,

            @NotNull(message = "El precio es obligatorio")
            @PositiveOrZero(message = "El precio no puede ser negativo")
            @Digits(integer = 10, fraction = 2, message = "El precio admite hasta 10 enteros y 2 decimales")
            BigDecimal price,

            @NotBlank(message = "La categoría es obligatoria")
            @Size(max = 50, message = "La categoría no puede superar los 50 caracteres")
            String category,

            @NotNull(message = "La condición del producto es obligatoria")
            ProductCondition condition,

            @NotNull(message = "La lista de imágenes no puede ser nula")
            @Size(max = 10, message = "No se pueden adjuntar más de 10 imágenes")
            List<
                    @NotBlank(message = "La URL de la imagen no puede estar vacía")
                    @URL(message = "Cada imagen debe tener formato de URL válido")
                            String> images,

            @NotNull(message = "El conjunto de productos no puede ser nulo")
            Set<
                    @NotNull(message = "El ID de producto no puede ser nulo")
                    @Positive(message = "El ID de producto debe ser un número positivo")
                            Long> productIds

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