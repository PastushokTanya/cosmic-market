package com.tpastushok.cosmocats.service.implementation;

import com.tpastushok.cosmocats.domain.product.Product;
import com.tpastushok.cosmocats.service.exception.NoSuchProductException;
import com.tpastushok.cosmocats.service.inerfaces.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class InMemoryProductService implements ProductService {
    private final List<Product> products = mockProductsList();

    @Override
    public List<Product> getProducts() {
        return products;
    }

    @Override
    public Product getProduct(UUID id) {
        return products.stream().filter(product -> product.getId().equals(id)).findFirst()
                .orElseThrow(() -> {
                    log.error("Product with id {} not found!", id);
                    return new NoSuchProductException("Product with id: " + id + " does not exist!");
                });
    }

    @Override
    public Product createProduct(Product product) {
        // Check if the product already exists by id
        Optional<Product> existingProduct = findProductById(product.getId());

        if (existingProduct.isPresent()) {
            // If the product exists, update it
            Product oldProduct = existingProduct.get();

            // Create a new Product with the updated values
            Product updatedProduct = Product.builder()
                    .categoryId(product.getCategoryId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .build();

            // Replace the old product with the updated one
            Product result = replaceProduct(oldProduct, updatedProduct);
            log.info("Product with id: {} updated successfully.", product.getId());
            return result; // Return the updated product
        } else {
            // If the product doesn't exist, add the new product to the list
            products.add(product);
            log.info("Product with id: {} created successfully.", product.getId());
            return product; // Return the newly added product
        }
    }

    @Override
    public Product updateProduct(UUID id, Product newProductData) {
        // Check if the product already exists by id
        Optional<Product> existingProduct = findProductById(id);

        if (existingProduct.isEmpty()) {
            String errorMessage = "Product with id: " + id + "does not exist. There is nothing to update!";
            log.error(errorMessage);
            throw new NoSuchProductException(errorMessage);
        } else {
            // Update fields of existed product
            Product oldProduct = existingProduct.get();

            Product updatedProduct = oldProduct.toBuilder()
                    .categoryId(newProductData.getCategoryId())
                    .name(newProductData.getName())
                    .description(newProductData.getDescription())
                    .price(newProductData.getPrice())
                    .build();

            replaceProduct(oldProduct, updatedProduct);
            log.info("Product with id: {} was updated successfully.", id);
            return updatedProduct;
        }
    }

    @Override
    public void deleteProduct(UUID id) {
        Product productToDelete = getProduct(id);
        products.remove(productToDelete);
        log.info("Product with id: {} was deleted successfully.", id);
    }

    private Optional<Product> findProductById(UUID id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    private Product replaceProduct(Product oldProduct, Product replacement) {
        products.remove(oldProduct);
        products.add(replacement);
        return replacement;
    }

    private List<Product> mockProductsList() {
        List<Product> result = List.of(
                Product.builder()
                        .id(UUID.fromString("77777777-0000-0000-0000-000000000001"))
                        .categoryId(UUID.randomUUID())
                        .name("Anti-Gravity Yarn Ball")
                        .description("A yarn ball that floats in zero gravity, perfect for cosmic playtime.")
                        .price(49.99)
                        .build(),

                Product.builder()
                        .id(UUID.fromString("77777777-0000-0000-0000-000000000002"))
                        .categoryId(UUID.randomUUID())
                        .name("Cosmic Milk")
                        .description("A refreshing drink made from milk harvested from cosmic cows.")
                        .price(15.99)
                        .build(),

                Product.builder()
                        .id(UUID.fromString("77777777-0000-0000-0000-000000000003"))
                        .categoryId(UUID.randomUUID())
                        .name("Stardust Blanket")
                        .description("A warm blanket infused with stardust for cozy nights in space.")
                        .price(99.99)
                        .build(),

                Product.builder()
                        .id(UUID.fromString("77777777-0000-0000-0000-000000000004"))
                        .categoryId(UUID.randomUUID())
                        .name("Galaxy Catnip")
                        .description("Specially cultivated catnip that provides a euphoric space experience.")
                        .price(12.99)
                        .build(),

                Product.builder()
                        .id(UUID.fromString("77777777-0000-0000-0000-000000000005"))
                        .categoryId(UUID.randomUUID())
                        .name("Nebula Scratching Post")
                        .description("A scratching post made from sturdy asteroid materials.")
                        .price(79.99)
                        .build()
        );

        //make result List mutable before its returning
        return new ArrayList<>(result);
    }
}
