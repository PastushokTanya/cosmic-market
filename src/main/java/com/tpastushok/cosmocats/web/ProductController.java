package com.tpastushok.cosmocats.web;

import com.tpastushok.cosmocats.dto.product.ProductCreationDto;
import com.tpastushok.cosmocats.dto.product.ProductDto;
import com.tpastushok.cosmocats.service.inerfaces.ProductService;
import com.tpastushok.cosmocats.web.mapper.ProductDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;
    private final ProductDtoMapper mapper;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getProducts() {
        return ResponseEntity.ok(mapper.toProductDto(service.getProducts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toProductDto(service.getProduct(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // update an existing Product
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
}