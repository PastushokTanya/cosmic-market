package com.tpastushok.cosmocats.service;

import com.tpastushok.cosmocats.domain.Category;
import com.tpastushok.cosmocats.domain.product.Product;
import com.tpastushok.cosmocats.repository.ProductRepository;
import com.tpastushok.cosmocats.repository.persistence.entity.ProductEntity;
import com.tpastushok.cosmocats.service.exception.NoSuchProductException;
import com.tpastushok.cosmocats.service.exception.PersistenceException;
import com.tpastushok.cosmocats.service.implementation.ProductServiceImpl;
import com.tpastushok.cosmocats.service.inerfaces.ProductService;
import com.tpastushok.cosmocats.web.mapper.ProductEntityMapper;
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

    @MockBean
    private ProductEntityMapper productEntityMapper;

    @Autowired
    private ProductService productService;

    @Captor
    private ArgumentCaptor<UUID> idCaptor;

    @Captor
    private ArgumentCaptor<Product> productArgumentCaptor;

    @Captor
    private ArgumentCaptor<ProductEntity> productEntityCaptor;

    private Product testProduct;
    private ProductEntity testProductEntity;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(UUID.fromString("77777777-0000-0000-0000-000000000001"))
                .category(Category.TOYS)
                .name("Anti-Gravity Yarn Ball")
                .description("A yarn ball that floats in zero gravity, perfect for cosmic playtime.")
                .price(49.99)
                .build();

        testProductEntity = ProductEntity.builder()
                .id(UUID.fromString("77777777-0000-0000-0000-000000000001"))
                .category(Category.TOYS)
                .name("Anti-Gravity Yarn Ball")
                .description("A yarn ball that floats in zero gravity, perfect for cosmic playtime.")
                .price(49.99)
                .build();
    }

    @Test
    void getAllProductsTest() {
        when(productRepository.findAll()).thenReturn(List.of(testProductEntity));
        when(productEntityMapper.toProducts(anyList())).thenReturn(List.of(testProduct));

        var result = productService.getProducts();

        assertEquals(1, result.size());
        assertEquals("Anti-Gravity Yarn Ball", result.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductByIdTest() {
        when(productRepository.findById(idCaptor.capture())).thenReturn(Optional.of(testProductEntity));
        when(productEntityMapper.toProduct(any())).thenReturn(testProduct);

        var result = productService.getProduct(testProduct.getId());

        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getId(), idCaptor.getValue());
    }

    @Test
    void getNonExistentProductByIdTest() {
        // Arrange
        when(productRepository.findById(idCaptor.capture())).thenReturn(Optional.empty());

        UUID nonExistentId = UUID.fromString("77777777-0000-0000-0000-000000000006");

        // Act & Assert
        assertThrows(NoSuchProductException.class, () -> productService.getProduct(nonExistentId));

        // Verify
        assertEquals(nonExistentId, idCaptor.getValue());
        verify(productRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void createProductTest() {
        // Mocking mapper behavior
        when(productEntityMapper.toProductEntity(testProduct)).thenReturn(testProductEntity);
        when(productEntityMapper.toProduct(testProductEntity)).thenReturn(testProduct);

        // Mocking repository behavior
        when(productRepository.save(productEntityCaptor.capture())).thenReturn(testProductEntity);

        // Call the service method
        var result = productService.createProduct(testProduct);

        // Assertions
        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getId(), productEntityCaptor.getValue().getId());

        // Verify interactions
        verify(productRepository, times(1)).save(productEntityCaptor.getValue());
        verify(productEntityMapper, times(1)).toProductEntity(testProduct);
        verify(productEntityMapper, times(1)).toProduct(testProductEntity);
    }

    @Test
    void updateProductTest() {
        // Arrange
        Product updatedProduct = testProduct.toBuilder().name("Updated Yarn Ball").build();

        when(productRepository.findById(idCaptor.capture())).thenReturn(Optional.of(testProductEntity));
        when(productRepository.save(productEntityCaptor.capture()))
                .thenReturn(testProductEntity.toBuilder().name("Updated Yarn Ball").build());
        when(productEntityMapper.toProduct(any())).thenReturn(updatedProduct);

        // Act
        var result = productService.updateProduct(testProduct.getId(), updatedProduct);

        // Assert
        assertEquals("Updated Yarn Ball", result.getName());
        assertEquals(testProduct.getId(), idCaptor.getValue());
        assertEquals("Updated Yarn Ball", productEntityCaptor.getValue().getName());
        verify(productRepository, times(1)).findById(testProduct.getId());
        verify(productRepository, times(1)).save(productEntityCaptor.getValue());
    }

    @Test
    void updateNonExistentProductTest() {
        // Given a non-existent product UUID
        UUID nonExistentProductId = UUID.fromString("77777777-0000-0000-0000-000000000006");

        // Create mock product data for the update
        Product updatedProductData = Product.builder()
                .id(nonExistentProductId)
                .category(Category.FOOD)
                .name("Updated Cosmic Milk")
                .description("Updated description")
                .price(19.99)
                .build();

        // Mock the repository to return Optional.empty() when attempting to get by the non-existent ID
        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        // Verify that updateProduct throws NoSuchProductException for the non-existent ID
        assertThrows(NoSuchProductException.class, () -> productService.updateProduct(nonExistentProductId, updatedProductData));
    }

    @Test
    void deleteProductTest() {
        doNothing().when(productRepository).deleteById(idCaptor.capture());
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProductEntity));

        productService.deleteProduct(testProduct.getId());

        assertEquals(testProduct.getId(), idCaptor.getValue());
        verify(productRepository, times(1)).deleteById(testProduct.getId());
    }

    @Test
    void deleteNonExistentProductTest() {
        // Given a non-existent product UUID
        UUID nonExistentProductId = UUID.fromString("77777777-0000-0000-0000-000000000006");

        // Simulate a PersistenceException with a NoSuchProductException as the cause
        NoSuchProductException causeException = new NoSuchProductException("Product with id: " + nonExistentProductId + " does not exist.");
        doThrow(new PersistenceException(causeException)).when(productRepository).deleteById(nonExistentProductId);


        // Verify that deleteProduct of the service throws a PersistenceException
        PersistenceException exception = assertThrows(PersistenceException.class, () -> productService.deleteProduct(nonExistentProductId));

        // Verify that the cause of the PersistenceException is a NoSuchProductException
        assertTrue(exception.getCause() instanceof NoSuchProductException);

        assertEquals(
                "Product with id: " + nonExistentProductId + " does not exist. There is nothing to delete!",
                exception.getCause().getMessage()
        );
    }
}
