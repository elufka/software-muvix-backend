package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;

import com.muvix.application.port.in.catalogue.SellableItemQueryUseCase.CatalogFilterQuery;
import com.muvix.application.port.in.catalogue.SellableItemQueryUseCase.CatalogPage;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.domain.model.catalogue.Batch;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.BatchJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.ProductJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.SellableItemJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.mapper.BatchPersistenceMapper;
import com.muvix.infrastructure.adapters.out.persistence.mysql.mapper.ProductPersistenceMapper;
import com.muvix.infrastructure.adapters.out.persistence.mysql.mapper.SellableItemPersistenceMapper;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.BatchJpaRepository;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.ProductJpaRepository;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.SellableItemJpaRepository;
import               com.muvix.infrastructure.adapters.out.persistence.mysql.repository.specification.SellableItemSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@Transactional
@RequiredArgsConstructor
public class SellableItemAdapter implements SellableItemRepositoryPort {

    private final SellableItemJpaRepository itemRepo;
    private final ProductJpaRepository productRepo;
    private final BatchJpaRepository batchRepo;

    @Override
    public SellableItem save(SellableItem item) {
        return switch (item) {
            case Product p -> ProductPersistenceMapper.toDomain(saveProduct(p));
            case Batch b -> BatchPersistenceMapper.toDomain(saveBatch(b));
        };
    }

    private ProductJpaEntity saveProduct(Product p) {
        if (p.getId() == null) {
            return productRepo.save(ProductPersistenceMapper.toEntity(p));
        }
        ProductJpaEntity managed = productRepo.findById(p.getId())
                .orElseThrow(() -> new DomainException("Producto no encontrado: " + p.getId()));
        ProductPersistenceMapper.updateEntity(p, managed);
        return productRepo.save(managed);
    }

    private BatchJpaEntity saveBatch(Batch b) {
        if (b.getId() == null) {
            return batchRepo.save(BatchPersistenceMapper.toEntity(b));
        }
        BatchJpaEntity managed = batchRepo.findById(b.getId())
                .orElseThrow(() -> new DomainException("Lote no encontrado: " + b.getId()));
        BatchPersistenceMapper.updateEntity(b, managed);
        return batchRepo.save(managed);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SellableItem> findById(Long id) {
        return itemRepo.findById(id).map(SellableItemPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findProductsByIds(Set<Long> productIds) {
        return productRepo.findAllByIdIn(productIds).stream()
                .map(ProductPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellableItem> findBySellerId(Long sellerId) {
        return itemRepo.findBySellerId(sellerId).stream()
                .map(SellableItemPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogPage searchPublicCatalog(CatalogFilterQuery query) {
        Sort sort = switch (query.sortBy()) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price");
            case TITLE_ASC -> Sort.by(Sort.Direction.ASC, "title");
            case TITLE_DESC -> Sort.by(Sort.Direction.DESC, "title");
            case NEWEST -> Sort.by(Sort.Direction.DESC, "id");
        };

        PageRequest pageable = PageRequest.of(query.page(), query.size(), sort);
        Page<SellableItemJpaEntity> resultPage = itemRepo.findAll(
                SellableItemSpecifications.fromFilter(query),
                pageable
        );

        List<SellableItem> domainItems = resultPage.getContent().stream()
                .map(SellableItemPersistenceMapper::toDomain)
                .toList();

        return new CatalogPage(
                domainItems,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );
    }
}
