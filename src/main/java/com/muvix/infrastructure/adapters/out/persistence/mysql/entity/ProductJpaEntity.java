package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.SellableItemJpaEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("PRODUCT")
class ProductJpaEntity extends SellableItemJpaEntity {
    private String category;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private Set<String> images = new HashSet<>();

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Set<String> getImages() { return images; }
    public void setImages(Set<String> images) { this.images = images; }
}