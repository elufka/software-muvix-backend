package com.muvix.application.usecase.catalogue;
import com.muvix.application.port.in.catalogue.ManageSellableItemUseCase;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.domain.model.catalogue.*;
import com.muvix.domain.model.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ManageSellableItemService implements ManageSellableItemUseCase {
    private final SellableItemRepositoryPort items;
    public ManageSellableItemService(SellableItemRepositoryPort items){this.items=items;}
    @Override @Transactional(readOnly=true) public SellableItem get(Long id,Long sellerId){return owned(id,sellerId);}
    @Override @Transactional public SellableItem replace(ReplaceCommand c){
        SellableItem item=owned(c.itemId(),c.sellerId());
        item.replaceDetails(c.title(),c.description(),c.price());
        if(item instanceof Product product) product.replaceProductDetails(c.category(),c.images());
        if(item instanceof Batch batch){
            if(c.productIds()!=null){
                for(Long id:c.productIds()){
                    SellableItem child=items.findById(id).orElseThrow(()->new DomainException("Batch product not found"));
                    if(!(child instanceof Product)||!child.getSellerId().equals(c.sellerId())||!child.isAvailable())
                        throw new DomainException("Batch members must be available products owned by the seller");
                }
                batch.replaceProducts(c.productIds());
            }
        }
        return items.save(item);
    }
    @Override @Transactional public void delete(Long id,Long sellerId){
        SellableItem item=owned(id,sellerId);
        if(item instanceof Batch batch){batch.deleteBatch();items.save(batch);return;}
        for(SellableItem sibling:items.findBySellerId(sellerId)){
            if(sibling instanceof Batch batch && batch.getState()==SellableItem.State.AVAILABLE && batch.contains(id)){
                batch.removeProduct(id);items.save(batch);
            }
        }
        item.softDelete();items.save(item);
    }
    private SellableItem owned(Long id,Long sellerId){
        SellableItem item=items.findById(id).orElseThrow(()->new DomainException("Sellable item not found"));
        if(!item.getSellerId().equals(sellerId))throw new DomainException("Only the seller can manage this item");
        return item;
    }
}
