package com.tpastushok.cosmocats.service.inerfaces;

import com.tpastushok.cosmocats.domain.CustomerType;
import com.tpastushok.cosmocats.domain.product.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<Product> getProducts();
    List<Product> getProductsForTargetAudience(CustomerType customerType);
    Product getProduct(UUID id);
    Product createProduct(Product product);
    Product updateProduct(UUID id, Product product);
    void deleteProduct(UUID id);
}