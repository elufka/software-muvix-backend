package com.muvix.infraestructure.adapters.persistence;

import com.muvix.domain.model.Publication;

public class PublicationEntityMapper {
    public static Publication toDomain(PublicationEntity entity) {
        if (entity == null) return null;
        return new Publication(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getItems(),
                entity.getQuantity(),
                entity.getPrice(),
                entity.getCreatedAt()
        );
    }

    public static PublicationEntity toEntity(Publication domain) {
        if (domain == null) return null;
        PublicationEntity entity = new PublicationEntity();
        entity.setId(domain.getId());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setItems(domain.getItems());
        entity.setQuantity(domain.getQuantity());
        entity.setPrice(domain.getPrice());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
