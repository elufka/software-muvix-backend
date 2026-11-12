package com.muvix.infrastructure.adapters.out.persistence.cassandra;

import com.muvix.application.port.in.catalogue.dto.CatalogItemReadModel;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Table("catalog_items_by_zone")
public class CatalogItemCassandraEntity {

    @PrimaryKeyColumn(name = "zone_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String zoneId;

    @PrimaryKeyColumn(name = "item_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String itemId;

    private String title;
    private String description;
    private BigDecimal price;
    private boolean isBatch;
    private String sellerId;
    private String eventId;
    private Instant createdAt;

    public CatalogItemReadModel toReadModel() {
        return new CatalogItemReadModel(
                this.itemId,
                this.zoneId,
                this.title,
                this.description,
                this.price,
                this.isBatch,
                this.sellerId,
                this.eventId,
                this.createdAt
        );
    }
}
