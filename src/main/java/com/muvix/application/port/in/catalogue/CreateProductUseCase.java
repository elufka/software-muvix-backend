package com.muvix.application.port.in.catalogue;

import com.muvix.domain.model.catalogue.Product;
import java.math.BigDecimal;
import java.util.List;

public interface CreateProductUseCase {

    record CreateProductCommand(
            Long sellerId,
            String title,
            String description,
            BigDecimal price, // 0 = Privado, >0 = Público
            String category,
            String zone,
            List<String> images,
            Long boxId // Opcional: Vincular directamente a una Box al crear
    ) {}

    Product createProduct(CreateProductCommand command);
}