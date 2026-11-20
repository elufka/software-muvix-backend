package com.muvix.infrastructure.adapters.out.persistence.mysql.repository.specification;

import com.muvix.application.port.in.catalogue.SellableItemQueryUseCase.CatalogFilterQuery;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.ProductJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.SellableItemJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.StateJpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class SellableItemSpecifications {

    private SellableItemSpecifications() {}

    public static Specification<SellableItemJpaEntity> fromFilter(CatalogFilterQuery query) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Regla base: solo ítems disponibles y públicos (precio > 0)
            predicates.add(cb.equal(root.get("state"), StateJpa.AVAILABLE));
            predicates.add(cb.greaterThan(root.get("price"), BigDecimal.ZERO));

            // Filtro por categoría (solo aplica a ProductJpaEntity mediante cb.treat)
            if (query.category() != null && !query.category().isBlank()) {
                var productRoot = cb.treat(root, ProductJpaEntity.class);
                predicates.add(cb.equal(
                        cb.lower(productRoot.get("category")),
                        query.category().trim().toLowerCase()
                ));
            }

            if (query.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), query.minPrice()));
            }
            if (query.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), query.maxPrice()));
            }
            if (query.condition() != null) {
                var productRoot = cb.treat(root, ProductJpaEntity.class);
                predicates.add(cb.equal(productRoot.get("condition"), query.condition()));
            }
            if (query.text() != null && !query.text().isBlank()) {
                String pattern = "%" + query.text().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[]{}));
        };
    }
}
