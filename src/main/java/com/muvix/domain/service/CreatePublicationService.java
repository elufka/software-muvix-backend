    package com.muvix.domain.service;

    import com.muvix.domain.model.Item;
    import com.muvix.domain.model.Publication;
    import com.muvix.domain.model.PublicationStatus;
    import com.muvix.domain.port.inbound.CreatePublicationUseCase;
    import com.muvix.domain.port.outbound.PublicationRepositoryPort;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;
    import java.util.List;

    public class CreatePublicationService implements CreatePublicationUseCase {
        private final PublicationRepositoryPort publicationRepositoryPort;

        public CreatePublicationService(PublicationRepositoryPort publicationRepositoryPort) {
            this.publicationRepositoryPort = publicationRepositoryPort;
        }

        @Override
        public Publication execute(String title, String description, Integer quantity, BigDecimal price, List<Item> items) {
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
            }

            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El precio debe ser mayor a cero.");
            }

            if (items == null || items.isEmpty()) {
                throw new IllegalArgumentException("Item de publicacion requerido para crearla");
            }

            Publication publication = new Publication(
                    null,
                    title,
                    description,
                    items,
                    quantity,
                    PublicationStatus.AVAILABLE,
                    price,
                    LocalDateTime.now());

            if (items != null) {
                for (Item item : items) {
                    item.setPublication(publication);
                }
            }

            return publicationRepositoryPort.save(publication);
        }
    }
