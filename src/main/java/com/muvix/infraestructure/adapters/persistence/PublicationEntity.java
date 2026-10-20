package com.muvix.infraestructure.adapters.persistence;

import com.muvix.domain.model.Item;
import com.muvix.domain.model.PublicationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PublicationEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private Integer quantity;
    private PublicationStatus status;
    private BigDecimal price;
    @OneToMany(mappedBy="item_id")
    private List<Item> items;
    private LocalDateTime createdAt;

    @PrePersist()
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        status = PublicationStatus.AVAILABLE;
    }
}
