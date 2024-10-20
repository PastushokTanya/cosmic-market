package com.tpastushok.cosmocats.service.implementation;

import com.tpastushok.cosmocats.data.ProductRepository;
import com.tpastushok.cosmocats.domain.product.Product;
import com.tpastushok.cosmocats.service.exception.NoSuchProductException;
import com.tpastushok.cosmocats.service.inerfaces.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> getProducts() {
        return productRepository.getAll();
    }

    @Override
    public Product getProduct(UUID id) {
        return productRepository.getById(id)
                .orElseThrow(() -> {
                    String errMsg = "Product with id: " + id + " does not exist!";
                    log.error(errMsg);
                    return new NoSuchProductException(errMsg);
                });
    }

    @Override
    public Product createProduct(Product product) {
        Product result = productRepository.addProduct(product);
        log.info("Product with id: {} created or updated successfully.", product.getId());
        return result;
    }

    @Override
    public Product updateProduct(UUID id, Product newProductData) {
        Product result;

        try {
            result = productRepository.update(id, newProductData);
        } catch (Throwable t) {
            log.error("Failed to update product with id: {}", id, t);
            throw t;
        }

        log.info("Product with id: {} was updated successfully.", result.getId());
        return result;
    }

    @Override
    public void deleteProduct(UUID id) {
        try {
            productRepository.delete(id);
        } catch (Throwable t) {
            log.error("Failed to delete product with id: {}", id, t);
            throw t;
        }
        log.info("Product with id: {} was deleted successfully.", id);
    }
}
