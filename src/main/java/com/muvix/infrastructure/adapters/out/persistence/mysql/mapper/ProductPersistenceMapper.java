package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.catalogue.Product;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.ProductJpaEntity;

import java.util.List;

public class ProductPersistenceMapper {
    private ProductPersistenceMapper() {}

    public static Product toDomain(ProductJpaEntity e) {
        return Product.reconstitute(
                e.getId(),
                e.getSellerId(),
                e.getTitle(),
                e.getDescription(),
                e.getPrice(),
                StateMapper.toDomain(e.getState()),
                e.getCategory(),
                e.getCondition(),
                List.copyOf(e.getImages())
        );
    }

    public static ProductJpaEntity toEntity(Product p) {
        ProductJpaEntity e = new ProductJpaEntity();
        updateEntity(p, e);
        return e;
    }

    public static void updateEntity(Product p, ProductJpaEntity e) {
        e.setSellerId(p.getSellerId());
        e.setTitle(p.getTitle());
        e.setDescription(p.getDescription());
        e.setPrice(p.getPrice());
        e.setCategory(p.getCategory());
        e.setCondition(p.getCondition());
        e.setState(StateMapper.toJpa(p.getState()));
        e.replaceImages(p.getImages());
    }
}
