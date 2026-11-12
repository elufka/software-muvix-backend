package com.muvix.application.port.out.persistence;

import com.muvix.domain.model.catalogue.Product;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.PublicBoxCassandraEntity;

import java.util.List;

public interface CatalogQueryPort {

    /** Obtiene todas las publicaciones de una zona determinada */
    List<CatalogItemCassandraEntity> findByZone(String zone);

    /** Obtiene publicaciones limitadas por zona para el feed principal */
    List<CatalogItemCassandraEntity> findByZoneWithLimit(String zone, int limit);

    /** Obtiene publicaciones de una zona filtradas por categoría */
    List<CatalogItemCassandraEntity> findByZoneAndCategory(String zone, String category);

    /** Obtiene las Boxes públicas asociadas a una zona */
    List<PublicBoxCassandraEntity> findBoxesByZone(String zone);
}