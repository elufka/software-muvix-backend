package com.muvix.application.usecase.catalogue;

import com.muvix.application.port.in.catalogue.BrowseCatalogUseCase;
import com.muvix.application.port.out.persistence.CatalogQueryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrowseCatalogService implements BrowseCatalogUseCase {

    private final CatalogQueryPort catalogQueryPort;

    public BrowseCatalogService(CatalogQueryPort catalogQueryPort) {
        this.catalogQueryPort = catalogQueryPort;
    }

    @Override
    public List<CatalogItemCassandraEntity> getFeedByZone(BrowseFeedQuery query) {
        if (query.zone() == null || query.zone().isBlank()) {
            throw new DomainException("Debes especificar una zona para consultar el catálogo.");
        }
        int limit = query.pageSize() > 0 ? query.pageSize() : 20;
        return catalogQueryPort.findByZoneWithLimit(query.zone(), limit);
    }
}