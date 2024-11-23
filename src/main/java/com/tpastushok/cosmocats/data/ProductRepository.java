package com.tpastushok.cosmocats.data;

import com.tpastushok.cosmocats.domain.product.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product addProduct(Product product);

    Optional<Product> getById(UUID id);

    Product update(UUID id, Product updated);

    void delete(UUID id);

    List<Product> getAll();
}
