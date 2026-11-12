package com.muvix.infrastructure.adapters.out.persistence.cassandra.repository;

import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogItemCassandraRepository extends CassandraRepository<CatalogItemCassandraEntity, String> {

    @Query("SELECT * FROM catalog_items_by_zone WHERE zone = ?0")
    List<CatalogItemCassandraEntity> findByZone(String zone);

    @Query("SELECT * FROM catalog_items_by_zone WHERE zone = ?0 LIMIT ?1")
    List<CatalogItemCassandraEntity> findByZoneLimit(String zone, int limit);

    @Query("SELECT * FROM catalog_items_by_zone WHERE zone = ?0 AND category = ?1 ALLOW FILTERING")
    List<CatalogItemCassandraEntity> findByZoneAndCategory(String zone, String category);
}
