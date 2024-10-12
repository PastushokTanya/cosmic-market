package com.tpastushok.cosmocats.service.implementation;

import com.tpastushok.cosmocats.domain.product.Product;
import com.tpastushok.cosmocats.service.exception.NoSuchProductException;
import com.tpastushok.cosmocats.service.inerfaces.ProductService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InMemoryProductService implements ProductService {
    private final List<Product> products = mockProductsList();

    @Override
    public List<Product> getProducts() {
        return products;
    }

    @Override
    public Product getProduct(UUID id) {
        return products.stream().filter(product -> product.getId().equals(id)).findFirst()
                .orElseThrow(() -> new NoSuchProductException("Product with id: " + id + "does not exist!"));
    }

    @Override
    public Product createProduct(Product product) {
        // Check if the product already exists by id
        Optional<Product> existingProduct = products.stream()
                .filter(p -> p.getId().equals(product.getId()))
                .findFirst();

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
            products.remove(oldProduct);
            products.add(updatedProduct);
            return updatedProduct; // Return the updated product
        } else {
            // If the product doesn't exist, add the new product to the list
            products.add(product);
            return product; // Return the newly added product
        }
    }

    @Override
    public void deleteProduct(UUID id) {
        Product productToDelete = getProduct(id);
        products.remove(productToDelete);
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
