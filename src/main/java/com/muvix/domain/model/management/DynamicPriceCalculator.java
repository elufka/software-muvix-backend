package com.muvix.domain.model.management;

import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class DynamicPriceCalculator {

    public BigDecimal calcular(SellableItem item, Instant fechaLimiteVenta, List<DiscountRule> reglas, Instant ahora) {
        if (!item.isPublic()) return BigDecimal.ZERO;
        if (ahora.isAfter(fechaLimiteVenta)) {
            throw new DomainException("El periodo de venta ha expirado");
        }

        long diasRestantes = Duration.between(ahora, fechaLimiteVenta).toDays();
        BigDecimal maxPorcentaje = BigDecimal.ZERO;

        for (DiscountRule regla : reglas) {
            if (diasRestantes <= regla.daysLeft()) {
                if (regla.percentage().compareTo(maxPorcentaje) > 0) {
                    maxPorcentaje = regla.percentage();
                }
            }
        }

        if (maxPorcentaje.signum() == 0) return item.getPrice();

        BigDecimal descuentoFactor = BigDecimal.ONE.subtract(
                maxPorcentaje.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
        );
        return item.getPrice().multiply(descuentoFactor).setScale(2, RoundingMode.HALF_UP);
    }
}
