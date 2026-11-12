package com.muvix.application.port.in.catalogue;

import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;

import java.util.List;

public interface BrowseCatalogUseCase {

    record BrowseFeedQuery(String zone, int pageSize) {}

    List<CatalogItemCassandraEntity> getFeedByZone(BrowseFeedQuery query);
}
