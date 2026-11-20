package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.SellableItemJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.ProductCondition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@DiscriminatorValue("PRODUCT")
@Getter @Setter
public class ProductJpaEntity extends SellableItemJpaEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition") // Nota: Evitamos llamarla "condition" solo porque es palabra reservada en MySQL
    private ProductCondition condition;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "position")
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();


    public void replaceImages(List<String> newImages) {
        this.images.clear();
        if (newImages != null) {
            this.images.addAll(newImages);
        }
    }
}