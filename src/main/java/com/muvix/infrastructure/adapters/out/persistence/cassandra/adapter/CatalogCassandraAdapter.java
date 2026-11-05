package com.muvix.infrastructure.adapters.out.persistence.cassandra.adapter;

import com.muvix.application.port.out.persistence.CatalogQueryPort;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.PublicBoxCassandraEntity;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.repository.CatalogItemCassandraRepository;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.repository.PublicBoxCassandraRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogCassandraAdapter implements CatalogQueryPort {

    private final CatalogItemCassandraRepository itemRepository;
    private final PublicBoxCassandraRepository boxRepository;

    public CatalogCassandraAdapter(CatalogItemCassandraRepository itemRepository,
                                   PublicBoxCassandraRepository boxRepository) {
        this.itemRepository = itemRepository;
        this.boxRepository = boxRepository;
    }

    @Override
    public List<CatalogItemCassandraEntity> findByZone(String zone) {
        return itemRepository.findByZone(zone);
    }

    @Override
    public List<CatalogItemCassandraEntity> findByZoneWithLimit(String zone, int limit) {
        return itemRepository.findByZoneLimit(zone, limit);
    }

    @Override
    public List<CatalogItemCassandraEntity> findByZoneAndCategory(String zone, String category) {
        return itemRepository.findByZoneAndCategory(zone, category);
    }

    @Override
    public List<PublicBoxCassandraEntity> findBoxesByZone(String zone) {
        return boxRepository.findByZone(zone);
    }
}