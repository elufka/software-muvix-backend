package com.muvix.infraestructure.adapters.persistence;

import com.muvix.domain.model.Publication;
import com.muvix.domain.port.outbound.PublicationRepositoryPort;

import java.util.Optional;

public class PublicationPersisenceAdapter implements PublicationRepositoryPort {
    private final SpringDataPublicationRepository springDataPublicationRepository;

    public PublicationPersisenceAdapter(SpringDataPublicationRepository springDataPublicationRepository) {
        this.springDataPublicationRepository = springDataPublicationRepository;
    }

    @Override
    public Publication save(Publication publication) {
        PublicationEntity entity = PublicationEntityMapper.toEntity(publication);
        PublicationEntity savedEntity = springDataPublicationRepository.save(entity);
        return PublicationEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Publication> findById(Long id) {
        return Optional.empty();
    }
}
