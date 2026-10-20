package com.muvix.domain.port.outbound;

import com.muvix.domain.model.Publication;

import java.util.Optional;

public interface PublicationRepositoryPort {
    Publication save(Publication publication);
    Optional<Publication> findById(Long id);

}
