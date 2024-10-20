package com.tpastushok.cosmocats.data;

import com.tpastushok.cosmocats.domain.product.Product;
import com.tpastushok.cosmocats.service.exception.NoSuchProductException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InMemoryProductRepository implements ProductRepository {
    private final List<Product> products = mockProductsList();

    @Override
    public Product addProduct(Product product) {
        // Check if the product already exists by id
        Optional<Product> existingProduct = getById(product.getId());

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
            return replaceProduct(oldProduct, updatedProduct); // Return the updated product
        } else {
            // If the product doesn't exist, add the new product to the list
            products.add(product);
            return product; // Return the newly added product
        }
    }

    @Override
    public Optional<Product> getById(UUID id) {
        return products.stream().filter(product -> product.getId().equals(id)).findFirst();
    }

    @Override
    public Product update(UUID id, Product newProductData) {
        // Check if the product already exists by id
        Optional<Product> existingProduct = getById(id);

        if (existingProduct.isEmpty()) {
            throw new NoSuchProductException("Product with id: " + id + "does not exist. There is nothing to update!");
        } else {
            // Update fields of existed product
            Product oldProduct = existingProduct.get();

            Product updatedProduct = oldProduct.toBuilder()
                    .categoryId(newProductData.getCategoryId())
                    .name(newProductData.getName())
                    .description(newProductData.getDescription())
                    .price(newProductData.getPrice())
                    .build();

            return replaceProduct(oldProduct, updatedProduct);
        }
    }

    @Override
    public void delete(UUID id) {
        Optional<Product> productToDelete = getById(id);
        if (productToDelete.isPresent()) {
            products.remove(productToDelete.get());
        } else {
            throw new NoSuchProductException("Product with id: " + id + "does not exist. There is nothing to delete!");
        }
    }

    @Override
    public List<Product> getAll() {
        return products;
    }

    private Product replaceProduct(Product oldProduct, Product replacement) {
        delete(oldProduct.getId());
        return addProduct(replacement);
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
