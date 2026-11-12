package com.muvix.application.port.in.catalogue;

import com.muvix.domain.model.catalogue.Batch;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface CreateBatchUseCase {

    record NewProductData(
            String title,
            String description,
            BigDecimal price, // 0 = producto privado exclusivo del lote, >0 = disponible individualmente
            String category,
            List<String> images
    ) {}

    record CreateBatchFromScratchCommand(
            Long sellerId,
            String batchTitle,
            String batchDescription,
            BigDecimal batchPrice,
            String zone,
            List<NewProductData> productsToCreate
    ) {}

    record CreateBatchFromExistingCommand(
            Long sellerId,
            String batchTitle,
            String batchDescription,
            BigDecimal batchPrice,
            String zone,
            Set<Long> existingProductIds
    ) {}

    Batch createFromScratch(CreateBatchFromScratchCommand command);
    Batch createFromExisting(CreateBatchFromExistingCommand command);
}