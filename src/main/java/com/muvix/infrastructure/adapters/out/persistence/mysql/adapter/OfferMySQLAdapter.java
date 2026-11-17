package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;
import com.muvix.application.port.out.persistence.OfferRepositoryPort;
import com.muvix.domain.model.negotiation.Offer;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.OfferJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.OfferJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
@Component @Transactional
public class OfferMySQLAdapter implements OfferRepositoryPort {
    private final OfferJpaRepository repository;
    public OfferMySQLAdapter(OfferJpaRepository repository){this.repository=repository;}
    @Override public Offer save(Offer offer){
        OfferJpaEntity entity=offer.getId()==null?new OfferJpaEntity():repository.findById(offer.getId()).orElseGet(OfferJpaEntity::new);
        entity.setId(offer.getId()); entity.setItemId(offer.getItemId()); entity.setBuyerId(offer.getBuyerId());
        entity.setAmount(offer.getAmount()); entity.setCreatedAt(offer.getCreatedAt()); entity.setStatus(offer.getStatus());
        return toDomain(repository.save(entity));
    }
    @Override @Transactional(readOnly=true) public Optional<Offer> findById(Long id){return repository.findById(id).map(OfferMySQLAdapter::toDomain);}
    private static Offer toDomain(OfferJpaEntity e){return Offer.restore(e.getId(),e.getItemId(),e.getBuyerId(),e.getAmount(),e.getCreatedAt(),e.getStatus());}
}
