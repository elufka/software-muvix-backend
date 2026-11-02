package com.muvix.infraestructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("BATCH")
class BatchJpaEntity extends SellableItemJpaEntity {

    @ElementCollection
    @CollectionTable(name = "batch_products", joinColumns = @JoinColumn(name = "batch_id"))
    @Column(name = "product_id")
    private Set<Long> productIds = new HashSet<>();

    public Set<Long> getProductIds() { return productIds; }
    public void setProductIds(Set<Long> productIds) { this.productIds = productIds; }
}
