package com.muvix.application.port.in.event;

import com.muvix.domain.model.logistic.Location;
import com.muvix.domain.model.management.DiscountRule;
import com.muvix.domain.model.management.RelocationEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface CreateRelocationEventUseCase {

    record CreateSaleBoxCommand(
            Long userId,
            String title,
            Location originLocation,
            Instant saleDeadline,
            BigDecimal takeItAllPrice,
            List<DiscountRule> discountRules
    ) {}

    record CreatePurchaseBoxCommand(
            Long userId,
            String title,
            Location destinationLocation,
            Instant arrivalDeliveryDate
    ) {}

    record CreateFullRelocationBoxCommand(
            Long userId,
            String title,
            Location originLocation,
            Location destinationLocation,
            Instant saleDeadline,
            Instant arrivalDeliveryDate,
            BigDecimal takeItAllPrice,
            List<DiscountRule> discountRules
    ) {}

    RelocationEvent createSaleBox(CreateSaleBoxCommand command);
    RelocationEvent createPurchaseBox(CreatePurchaseBoxCommand command);
    RelocationEvent createFullRelocationBox(CreateFullRelocationBoxCommand command);
}