package com.tpastushok.cosmocats.web;

import com.tpastushok.cosmocats.dto.competitors.observer.CompetitorsObserverResponseDto;
import com.tpastushok.cosmocats.dto.product.ProductCreationDto;
import com.tpastushok.cosmocats.dto.product.ProductDto;
import com.tpastushok.cosmocats.featuretoggle.FeatureToggles;
import com.tpastushok.cosmocats.featuretoggle.annotation.FeatureToggle;
import com.tpastushok.cosmocats.service.inerfaces.CompetitorObserverService;
import com.tpastushok.cosmocats.service.inerfaces.ProductService;
import com.tpastushok.cosmocats.web.mapper.ProductDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.tpastushok.cosmocats.domain.CustomerType.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;
    private final ProductDtoMapper mapper;
    private final CompetitorObserverService competitorObserverService;

    @PreAuthorize("hasRole('COSMO_ADMIN')")
    @GetMapping
    public ResponseEntity<List<ProductDto>> getProducts() {
        return ResponseEntity.ok(mapper.toProductDto(service.getProducts()));
    }

    @GetMapping("/kitty-products")
    @FeatureToggle(FeatureToggles.KITTY_PRODUCTS)
    public ResponseEntity<List<ProductDto>> getProductsForKitties() {
        return ResponseEntity.ok(
                mapper.toProductDto(
                        service.getProductsForTargetAudience(KITTY)
                )
        );
    }

    @GetMapping("/junior-cat-products")
    @FeatureToggle(FeatureToggles.JUNIOR_CAT_PRODUCTS)
    public ResponseEntity<List<ProductDto>> getProductsForJuniorCats() {
        return ResponseEntity.ok(
                mapper.toProductDto(
                        service.getProductsForTargetAudience(JUNIOR_CAT)
                )
        );
    }

    @GetMapping("/senior-cat-products")
    @FeatureToggle(FeatureToggles.SENIOR_CAT_PRODUCTS)
    public ResponseEntity<List<ProductDto>> getProductsForSeniorCats() {
        return ResponseEntity.ok(
                mapper.toProductDto(
                        service.getProductsForTargetAudience(SENIOR_CAT)
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toProductDto(service.getProduct(id)));
    }

    @PreAuthorize("hasRole('COSMO_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // update an existing Product

    @PreAuthorize("hasRole('COSMO_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable UUID id, @RequestBody @Valid ProductCreationDto productDto) {
        return ResponseEntity.ok(
                mapper.toProductDto(
                        service.updateProduct(id, mapper.toProduct(productDto)
                        )
                )
        );
    }

    // create new Product
    @PreAuthorize("hasRole('COSMO_ADMIN')")
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductCreationDto productCreationDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                mapper.toProductDto(
                        service.createProduct(
                                mapper.toProduct(productCreationDto)
                        )
                )
        );
    }

    @PreAuthorize("hasRole('COSMO_MARKETOLOGIST')")
    @GetMapping("/{id}/competitor-price-observer")
    public ResponseEntity<CompetitorsObserverResponseDto> getOtherStoresPrices(@PathVariable UUID id) {
        return ResponseEntity.ok(competitorObserverService.observeOtherStorePrices(id));
    }
}