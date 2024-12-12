package com.tpastushok.cosmocats.service.implementation;

import com.tpastushok.cosmocats.repository.ProductRepository;
import com.tpastushok.cosmocats.repository.persistence.entity.ProductEntity;
import com.tpastushok.cosmocats.domain.CustomerType;
import com.tpastushok.cosmocats.domain.product.Product;
import com.tpastushok.cosmocats.service.exception.NoSuchProductException;
import com.tpastushok.cosmocats.service.exception.PersistenceException;
import com.tpastushok.cosmocats.service.inerfaces.ProductService;
import com.tpastushok.cosmocats.web.mapper.ProductEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final ProductEntityMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return mapper.toProducts(productRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsForTargetAudience(CustomerType customerType) {
        return getProducts().stream().filter(p -> p.getTargetAudience() == customerType).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProduct(UUID id) {
        Optional<ProductEntity> result = productRepository.findById(id);
        if (result.isEmpty()) {
            throw new NoSuchProductException("Product with id: " + id + " does not exist!");
        } else {
            return mapper.toProduct(result.get());
        }
    }

    @Override
    @Transactional
    public Product createProduct(Product product) {
        Product result = mapper.toProduct(productRepository.save(mapper.toProductEntity(product)));

        log.info("Product with id: {} created or updated successfully.", product.getId());
        return result;
    }

    @Override
    @Transactional
    public Product updateProduct(UUID id, Product newProductData) {
        ProductEntity existingProductEntity = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchProductException("Can't find product with id: " + id));

        existingProductEntity = existingProductEntity.toBuilder()
                .category(newProductData.getCategory())
                .name(newProductData.getName())
                .description(newProductData.getDescription())
                .price(newProductData.getPrice())
                .targetAudience(newProductData.getTargetAudience())
                .build();

        Product result;

        try {
            result = mapper.toProduct(productRepository.save(existingProductEntity));
        } catch (Throwable t) {
            log.error("Failed to update product with id: {}", id, t);
            throw new PersistenceException(t);
        }

        log.info("Product with id: {} was updated successfully.", result.getId());
        return result;
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        try {
            // if such Product doesn't exist, the exception will be thrown
            productRepository.findById(id).orElseThrow(
                    () -> new NoSuchProductException("Product with id: " + id + " does not exist. There is nothing to delete!")
            );

            productRepository.deleteById(id);
        } catch (Throwable t) {
            log.error("Failed to delete product with id: {}", id, t);
            throw new PersistenceException(t);
        }
        log.info("Product with id: {} was deleted successfully.", id);
    }
}
