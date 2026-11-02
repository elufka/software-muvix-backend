package com.muvix.infraestructure.adapters.out.persistence.cassandra.entity;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Table("catalog_items_by_zone")
public class CatalogItemCassandraEntity {

    @PrimaryKeyColumn(name = "zone", type = PrimaryKeyType.PARTITIONED)
    private String zone;

    @PrimaryKeyColumn(name = "created_at", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Instant createdAt;

    @PrimaryKeyColumn(name = "item_id", type = PrimaryKeyType.CLUSTERED)
    private Long itemId;

    @Column("seller_id")
    private Long sellerId;

    @Column("seller_name")
    private String sellerName;

    @Column("title")
    private String title;

    @Column("description")
    private String description;

    @Column("original_price")
    private BigDecimal originalPrice;

    @Column("current_dynamic_price")
    private BigDecimal currentDynamicPrice;

    @Column("item_type")
    private String itemType; // PRODUCT / BATCH

    @Column("category")
    private String category;

    @Column("main_image_url")
    private String mainImageUrl;

    @Column("is_take_it_all_eligible")
    private boolean isTakeItAllEligible;

    @Column("box_id")
    private Long boxId;

    // Constructores, Getters y Setters
    public CatalogItemCassandraEntity() {}

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public BigDecimal getCurrentDynamicPrice() { return currentDynamicPrice; }
    public void setCurrentDynamicPrice(BigDecimal currentDynamicPrice) { this.currentDynamicPrice = currentDynamicPrice; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMainImageUrl() { return mainImageUrl; }
    public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
    public boolean isTakeItAllEligible() { return isTakeItAllEligible; }
    public void setTakeItAllEligible(boolean takeItAllEligible) { isTakeItAllEligible = takeItAllEligible; }
    public Long getBoxId() { return boxId; }
    public void setBoxId(Long boxId) { this.boxId = boxId; }
}