package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.catalogue.Product;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.ProductJpaEntity;

import java.util.List;

public final class ProductPersistenceMapper {
    private ProductPersistenceMapper() {}

    public static Product toDomain(ProductJpaEntity e) {
        return Product.reconstitute(
                e.getId(), e.getSellerId(), e.getTitle(), e.getDescription(),
                e.getPrice(), StateMapper.toDomain(e.getState()),
                e.getCategory(), List.copyOf(e.getImages()));   // List.copyOf evita que el dominio quede atado a un PersistentBag de Hibernate
    }

    // Para ítems nuevos
    public static ProductJpaEntity toEntity(Product p) {
        ProductJpaEntity e = new ProductJpaEntity();
        updateEntity(p, e);
        return e;
    }

    // Para ítems existentes: copia los campos sobre una entidad ya gestionada por JPA.
    public static void updateEntity(Product p, ProductJpaEntity e) {
        e.setSellerId(p.getSellerId());
        e.setTitle(p.getTitle());
        e.setDescription(p.getDescription());
        e.setPrice(p.getPrice());
        e.setState(StateMapper.toJpa(p.getState()));
        e.setCategory(p.getCategory());
        e.getImages().clear();
        e.getImages().addAll(p.getImages());
        /* clear() + addAll() en lugar de setImages(...)
        evita el error A collection with cascade="all-delete-orphan" was no longer referenced.*/
    }
}