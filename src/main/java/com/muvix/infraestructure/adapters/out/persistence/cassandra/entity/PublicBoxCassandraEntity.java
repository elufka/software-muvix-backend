package com.muvix.infraestructure.adapters.out.persistence.cassandra.entity;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("public_boxes_by_zone")
public class PublicBoxCassandraEntity {

    @PrimaryKeyColumn(name = "zone", type = PrimaryKeyType.PARTITIONED)
    private String zone;

    @PrimaryKeyColumn(name = "sale_deadline", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private Instant saleDeadline;

    @PrimaryKeyColumn(name = "box_id", type = PrimaryKeyType.CLUSTERED)
    private Long boxId;

    @Column("owner_id")
    private Long ownerId;

    @Column("owner_name")
    private String ownerName;

    @Column("title")
    private String title;

    @Column("take_it_all_price")
    private BigDecimal takeItAllPrice;

    @Column("total_items_count")
    private int totalItemsCount;

    @Column("box_type")
    private String boxType; // SALE / FULL_RELOCATION

    // Constructores, Getters y Setters
    public PublicBoxCassandraEntity() {}

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public Instant getSaleDeadline() { return saleDeadline; }
    public void setSaleDeadline(Instant saleDeadline) { this.saleDeadline = saleDeadline; }
    public Long getBoxId() { return boxId; }
    public void setBoxId(Long boxId) { this.boxId = boxId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getTakeItAllPrice() { return takeItAllPrice; }
    public void setTakeItAllPrice(BigDecimal takeItAllPrice) { this.takeItAllPrice = takeItAllPrice; }
    public int getTotalItemsCount() { return totalItemsCount; }
    public void setTotalItemsCount(int totalItemsCount) { this.totalItemsCount = totalItemsCount; }
    public String getBoxType() { return boxType; }
    public void setBoxType(String boxType) { this.boxType = boxType; }
}