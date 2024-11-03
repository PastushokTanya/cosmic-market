package com.tpastushok.cosmocats.service;

import com.tpastushok.cosmocats.data.ProductRepository;
import com.tpastushok.cosmocats.domain.product.Product;
import com.tpastushok.cosmocats.service.exception.NoSuchProductException;
import com.tpastushok.cosmocats.service.implementation.ProductServiceImpl;
import com.tpastushok.cosmocats.service.inerfaces.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {ProductServiceImpl.class})
public class ProductServiceTest {

    @MockBean
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Captor
    private ArgumentCaptor<UUID> idCaptor;

    @Captor
    private ArgumentCaptor<Product> productArgumentCaptor;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(UUID.fromString("77777777-0000-0000-0000-000000000001"))
                .categoryId(UUID.randomUUID())
                .name("Anti-Gravity Yarn Ball")
                .description("A yarn ball that floats in zero gravity, perfect for cosmic playtime.")
                .price(49.99)
                .build();
    }

    @Test
    void getAllProductsTest() {
        when(productRepository.getAll()).thenReturn(List.of(testProduct));

        var result = productService.getProducts();

        assertEquals(1, result.size());
        assertEquals("Anti-Gravity Yarn Ball", result.get(0).getName());
        verify(productRepository, times(1)).getAll();
    }

    @Test
    void getProductByIdTest() {
        when(productRepository.getById(idCaptor.capture())).thenReturn(Optional.of(testProduct));

        var result = productService.getProduct(testProduct.getId());

        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getId(), idCaptor.getValue());
    }

    @Test
    void getNonExistentProductByIdTest() {
        when(productRepository.getById(any(UUID.class))).thenReturn(Optional.empty());

        UUID nonExistentId = UUID.fromString("77777777-0000-0000-0000-000000000006");
        assertThrows(NoSuchProductException.class, () -> productService.getProduct(nonExistentId));
    }

    @Test
    void createProductTest() {
        when(productRepository.addProduct(productArgumentCaptor.capture())).thenReturn(testProduct);

        var result = productService.createProduct(testProduct);

        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getId(), productArgumentCaptor.getValue().getId());
        verify(productRepository, times(1)).addProduct(testProduct);
    }

    @Test
    void updateProductTest() {
        Product updatedProduct = testProduct.toBuilder().name("Updated Yarn Ball").build();

        when(productRepository.update(idCaptor.capture(), productArgumentCaptor.capture()))
                .thenReturn(updatedProduct);
        when(productRepository.getById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        var result = productService.updateProduct(testProduct.getId(), updatedProduct);

        assertEquals("Updated Yarn Ball", result.getName());
        assertEquals(testProduct.getId(), idCaptor.getValue());
        assertEquals("Updated Yarn Ball", productArgumentCaptor.getValue().getName());
        verify(productRepository, times(1)).update(testProduct.getId(), updatedProduct);
    }

    @Test
    void updateNonExistentProductTest() {
        // Given a non-existent product UUID
        UUID nonExistentProductId = UUID.fromString("77777777-0000-0000-0000-000000000006");

        // Create mock product data for the update
        Product updatedProductData = Product.builder()
                .id(nonExistentProductId)
                .categoryId(UUID.randomUUID())
                .name("Updated Cosmic Milk")
                .description("Updated description")
                .price(19.99)
                .build();

        // Mock the repository to return Optional.empty() when attempting to get by the non-existent ID
        when(productRepository.getById(nonExistentProductId)).thenReturn(Optional.empty());

        // Simulate NoSuchProductException when attempting to update a non-existent product
        doThrow(new NoSuchProductException("Product with id: " + nonExistentProductId + " does not exist."))
                .when(productRepository).update(nonExistentProductId, updatedProductData);

        // Verify that updateProduct throws NoSuchProductException for the non-existent ID
        assertThrows(NoSuchProductException.class, () -> productService.updateProduct(nonExistentProductId, updatedProductData));
    }

    @Test
    void deleteProductTest() {
        when(productRepository.getById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(idCaptor.capture());

        productService.deleteProduct(testProduct.getId());

        assertEquals(testProduct.getId(), idCaptor.getValue());
        verify(productRepository, times(1)).delete(testProduct.getId());
    }

    @Test
    void deleteNonExistentProductTest() {
        // Given a non-existent product UUID
        UUID nonExistentProductId = UUID.fromString("77777777-0000-0000-0000-000000000006");

        // Simulate NoSuchProductException when attempting to delete a product with the non-existent ID
        doThrow(new NoSuchProductException("Product with id: " + nonExistentProductId + " does not exist."))
                .when(productRepository).delete(nonExistentProductId);

        // Verify that deleteProduct of the service rethrows NoSuchProductException for the non-existent ID
        assertThrows(NoSuchProductException.class, () -> productService.deleteProduct(nonExistentProductId));
    }

    // Utility to create a mock list of products for repeated tests
    private List<Product> mockProductsList() {
        return List.of(
                testProduct,
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
                        .build()
        );
    }
}
