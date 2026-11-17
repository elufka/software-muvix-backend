package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
@Entity @Table(name="shopping_carts")
public class ShoppingCartJpaEntity {
    @Id @Column(name="buyer_id") private Long buyerId;
    @ElementCollection @CollectionTable(name="shopping_cart_items",joinColumns=@JoinColumn(name="buyer_id"))
    @Column(name="item_id",nullable=false) private Set<Long> itemIds=new HashSet<>();
    public Long getBuyerId(){return buyerId;} public void setBuyerId(Long buyerId){this.buyerId=buyerId;}
    public Set<Long> getItemIds(){return itemIds;} public void setItemIds(Set<Long> itemIds){this.itemIds=itemIds;}
}
