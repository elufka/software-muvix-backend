package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.catalogue.*;
import com.muvix.application.port.in.catalogue.SellableItemQueryUseCase.CatalogFilterQuery;
import com.muvix.application.port.in.catalogue.SellableItemQueryUseCase.SortOption;
import com.muvix.domain.model.catalogue.Batch;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.infrastructure.adapters.in.rest.controller.publication.dto.PublicationDtos.*;
import com.muvix.infrastructure.adapters.in.rest.controller.publication.mapper.PublicationRestMapper;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.ProductCondition;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/publication")
public class SellableItemController {

    private final CreateProductUseCase createProduct;
    private final CreateBatchUseCase createBatch;
    private final ManageSellableItemUseCase manageItems;
    private final SellableItemQueryUseCase queryItems;
    private final PublicationRestMapper mapper;

    public SellableItemController(CreateProductUseCase createProduct,
                                  CreateBatchUseCase createBatch,
                                  ManageSellableItemUseCase manageItems,
                                  SellableItemQueryUseCase queryItems,
                                  PublicationRestMapper mapper) {
        this.createProduct = createProduct;
        this.createBatch = createBatch;
        this.manageItems = manageItems;
        this.queryItems = queryItems;
        this.mapper = mapper;
    }

    @PostMapping("/individual")
    public ResponseEntity<PublicationResponse> createProduct(@AuthenticationPrincipal Jwt jwt,
                                                             @Valid @RequestBody CreateProductRequest r) {
        Product saved = createProduct.createProduct(mapper.toCommand(userId(jwt), r));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(saved));
    }

    @PostMapping("/batch")
    public ResponseEntity<PublicationResponse> createBatch(@AuthenticationPrincipal Jwt jwt,
                                                           @Valid @RequestBody CreateBatchRequest r) {
        Long sellerId = userId(jwt);
        Batch saved = (r.existingProductIds() == null || r.existingProductIds().isEmpty())
                ? createBatch.createFromScratch(mapper.toScratchCommand(sellerId, r))
                : createBatch.createFromExisting(mapper.toExistingCommand(sellerId, r));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<CatalogPageResponse> searchPublicCatalog(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) ProductCondition condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "NEWEST") SortOption sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var query = new CatalogFilterQuery(text, zone, category,condition, minPrice, maxPrice, sortBy, page, size);
        return ResponseEntity.ok(mapper.toPageResponse(queryItems.searchPublicCatalog(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicationResponse> getPublicById(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(queryItems.getPublicById(id)));
    }

    @GetMapping("/me")
    public ResponseEntity<List<PublicationResponse>> getMyPublications(@AuthenticationPrincipal Jwt jwt) {
        List<PublicationResponse> items = queryItems.getBySellerId(userId(jwt)).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<PublicationResponse> getMyPublicationById(@AuthenticationPrincipal Jwt jwt,
                                                                    @PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(queryItems.getOwnedById(id, userId(jwt))));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<PublicationResponse> replace(@AuthenticationPrincipal Jwt jwt,
                                                       @PathVariable Long id,
                                                       @Valid @RequestBody ReplaceItemRequest r) {
        var updated = manageItems.replace(mapper.toReplaceCommand(id, userId(jwt), r));
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        manageItems.delete(id, userId(jwt));
        return ResponseEntity.noContent().build();
    }

    private static Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
