package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.catalogue.*;
import com.muvix.domain.model.catalogue.Batch;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.domain.model.catalogue.SellableItem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
public class SellableItemController {
    private final CreateProductUseCase createProduct;
    private final CreateBatchUseCase createBatch;
    private final ManageSellableItemUseCase manageItems;
    public SellableItemController(CreateProductUseCase createProduct, CreateBatchUseCase createBatch,
                                  ManageSellableItemUseCase manageItems) {
        this.createProduct=createProduct; this.createBatch=createBatch; this.manageItems=manageItems;
    }
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@AuthenticationPrincipal Jwt jwt,@RequestBody CreateProductRequest r){
        Product saved=createProduct.createProduct(new CreateProductUseCase.CreateProductCommand(userId(jwt),r.title(),r.description(),r.price(),r.category(),r.zone(),r.images(),r.boxId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    @PostMapping("/batches")
    public ResponseEntity<Batch> createBatch(@AuthenticationPrincipal Jwt jwt,@RequestBody CreateBatchRequest r){
        Batch saved= r.existingProductIds()==null||r.existingProductIds().isEmpty()
                ? createBatch.createFromScratch(new CreateBatchUseCase.CreateBatchFromScratchCommand(userId(jwt),r.title(),r.description(),r.price(),r.zone(),r.productsToCreate()))
                : createBatch.createFromExisting(new CreateBatchUseCase.CreateBatchFromExistingCommand(userId(jwt),r.title(),r.description(),r.price(),r.zone(),r.existingProductIds()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    @GetMapping("/items/{id}")
    public ResponseEntity<SellableItem> get(@AuthenticationPrincipal Jwt jwt,@PathVariable Long id){return ResponseEntity.ok(manageItems.get(id,userId(jwt)));}
    @PutMapping("/items/{id}")
    public ResponseEntity<SellableItem> replace(@AuthenticationPrincipal Jwt jwt,@PathVariable Long id,@RequestBody ReplaceItemRequest r){
        return ResponseEntity.ok(manageItems.replace(new ManageSellableItemUseCase.ReplaceCommand(id,userId(jwt),r.title(),r.description(),r.price(),r.category(),r.images(),r.productIds())));
    }
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt,@PathVariable Long id){manageItems.delete(id,userId(jwt));return ResponseEntity.noContent().build();}
    private static Long userId(Jwt jwt){return Long.valueOf(jwt.getSubject());}
    public record CreateProductRequest(String title,String description,BigDecimal price,String category,String zone,List<String> images,Long boxId){}
    public record CreateBatchRequest(String title,String description,BigDecimal price,String zone,List<CreateBatchUseCase.NewProductData> productsToCreate,Set<Long> existingProductIds){}
    public record ReplaceItemRequest(String title,String description,BigDecimal price,String category,List<String> images,Set<Long> productIds){}
}
