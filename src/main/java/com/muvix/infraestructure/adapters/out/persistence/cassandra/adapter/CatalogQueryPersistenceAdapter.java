package com.muvix.infraestructure.adapters.out.persistence.cassandra.adapter;

import com.muvix.infraestructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;
import com.muvix.infraestructure.adapters.out.persistence.cassandra.repository.CatalogItemCassandraRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogQueryPersistenceAdapter {

    private final CatalogItemCassandraRepository cassandraRepository;

    public CatalogQueryPersistenceAdapter(CatalogItemCassandraRepository cassandraRepository) {
        this.cassandraRepository = cassandraRepository;
    }

    /**
     * Consulta ultrarrápida optimizada para el feed de la Homepage filtrado por Zona
     */
    public List<CatalogItemCassandraEntity> getHomePageFeedByZone(String zone, int limit) {
        return cassandraRepository.findByZoneLimit(zone, limit);
    }
}
