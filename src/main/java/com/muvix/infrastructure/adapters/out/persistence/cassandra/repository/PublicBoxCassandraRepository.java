package com.muvix.infrastructure.adapters.out.persistence.cassandra.repository;

import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.PublicBoxCassandraEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicBoxCassandraRepository extends CassandraRepository<PublicBoxCassandraEntity, String> {

    @Query("SELECT * FROM public_boxes_by_zone WHERE zone = ?0")
    List<PublicBoxCassandraEntity> findByZone(String zone);
}
