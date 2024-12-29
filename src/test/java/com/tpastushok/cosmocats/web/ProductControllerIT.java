package com.tpastushok.cosmocats.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpastushok.cosmocats.AbstractIt;
import com.tpastushok.cosmocats.domain.Category;
import com.tpastushok.cosmocats.domain.product.Product;
import com.tpastushok.cosmocats.dto.product.ProductCreationDto;
import com.tpastushok.cosmocats.dto.product.ProductDto;
import com.tpastushok.cosmocats.repository.ProductRepository;
import com.tpastushok.cosmocats.service.inerfaces.ProductService;
import com.tpastushok.cosmocats.web.mapper.ProductDtoMapper;
import com.tpastushok.cosmocats.web.mapper.ProductEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static com.tpastushok.cosmocats.domain.CustomerType.SENIOR_CAT;
import static com.tpastushok.cosmocats.util.SecurityUtil.API_KEY_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
/**
 * @DirtiesContext is used to indicate that the application context should be
 * marked as dirty and should be reloaded after the test method or class finishes execution.
 *
 * This is particularly useful in integration tests where the application context
 * may be modified during the test, such as when the test data or the environment
 * is changed. Marking the context as dirty ensures that the next test starts with
 * a fresh context, avoiding any potential side effects or state leaks between tests.
 *
 * In the case of this `ProductControllerIT` test class, @DirtiesContext ensures that
 * any changes made to the context (e.g., test data) do not affect other tests, ensuring
 * test isolation and consistency across multiple test runs.
 */
@DirtiesContext
/**
 * @AutoConfigureMockMvc is used to automatically configure and initialize MockMvc for
 * testing web layers in Spring Boot tests.
 *
 * This annotation enables the creation of an instance of `MockMvc`, which provides
 * a convenient way to perform HTTP requests (like GET, POST, etc.) and assert
 * the results in integration tests. It allows testing the controller layer (e.g.,
 * ProductController) WITHOUT STARTING THE FULL APPLICATION CONTEXT, providing
 * lightweight and fast testing for the web layer.
 *
 * In this case, @AutoConfigureMockMvc ensures that the `MockMvc` instance is available
 * for sending HTTP requests to the `/api/v1/products` endpoint in the integration test,
 * making it easier to verify the behavior of the product controller (e.g., creating
 * products, getting product data).
 */
@AutoConfigureMockMvc
public class ProductControllerIT extends AbstractIt {

    private static final String BASE_URL = "/api/v1/products";

    public static final String BEARER_TOKEN_STUB = "Bearer token stub";

    private final ProductCreationDto newProductData = ProductCreationDto.builder()
            .category("GADGETS")
            .name("Intergalactic Soap")
            .description("A soap that works in zero gravity.")
            .price(299.99)
            .targetAudience(SENIOR_CAT)
            .build();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductEntityMapper entityMapper;

    @Autowired
    private ProductDtoMapper dtoMapper;

    @SpyBean
    private ProductService productService;

    @MockBean
    JwtDecoder jwtDecoder;

    @BeforeEach
    void setup() {
        Mockito.reset(productService);
        productRepository.deleteAll();

        // Add sample data to the repository
        Product sampleProduct = Product.builder()
                .id(UUID.randomUUID())
                .category(Category.GADGETS)
                .name("Sample Cosmic Product")
                .description("Sample Celestial Description")
                .price(199.99)
                .targetAudience(SENIOR_CAT)
                .build();
        productRepository.save(entityMapper.toProductEntity(sampleProduct));
    }

    /**
     * Test retrieving all products.
     * Ensures that a GET request to the /products endpoint returns
     * a list of all products in the repository with an OK status.
     */
    @WithMockUser(roles = "COSMO_ADMIN")
    @Test
    void getAllProducts_shouldReturnAllProducts() throws Exception {
        var products = entityMapper.toProducts(productRepository.findAll());

        mockMvc.perform(get(BASE_URL))
                .andExpectAll(
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(dtoMapper.toProductDto(products)))
                );
    }

    /**
     * Test retrieving a product by ID.
     * Verifies that a product can be fetched by its ID and checks
     * that the response contains expected fields such as 'name' and 'description'.
     */
    @WithMockUser
    @Test
    void getProductById_shouldReturnProductDetails() throws Exception {
        // Retrieve the first product from the repository
        var existingProduct = StreamSupport.stream(productRepository.findAll().spliterator(), false)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No Product found! Impossible to complete the test."));

        mockMvc.perform(get(BASE_URL + "/{id}", existingProduct.getId()))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.name").value(existingProduct.getName()),
                        jsonPath("$.description").value(existingProduct.getDescription())
                );
    }

    /**
     * Test retrieving a non-existent product by ID.
     * Attempts to fetch a product that does not exist, verifying
     * that the server responds with a 404 Not Found status and an error message.
     */
    @WithMockUser
    @Test
    void getProductById_whenProductDoesNotExist_shouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get(BASE_URL + "/{id}", nonExistentId))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.title").value("Product Not Found"),
                        jsonPath("$.status").value(404)
                );
    }

    /**
     * Test deleting a product by ID.
     * Verifies that a product can be successfully deleted using its ID.
     * Confirms that the product is no longer present in the repository.
     */
    @WithMockUser(roles = "COSMO_ADMIN")
    @Test
    void deleteProductById_shouldRemoveProductFromRepository() throws Exception {
        // Retrieve the first product from the repository
        var productToDelete = StreamSupport.stream(productRepository.findAll().spliterator(), false)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No Product found! Impossible to complete the test."));

        // Perform the DELETE request
        mockMvc.perform(delete(BASE_URL + "/{id}", productToDelete.getId())
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Assert that the product is no longer in the repository
        boolean isProductDeleted = StreamSupport.stream(productRepository.findAll().spliterator(), false)
                .noneMatch(product -> product.getId().equals(productToDelete.getId()));

        assertThat(isProductDeleted).isTrue();
    }

    /**
     * Test for creating a new product.
     * <p>
     * This test verifies that a product can be successfully created via a POST request to the /api/v1/products endpoint.
     * The test performs the following steps:
     * <p>
     * 1. Sends a POST request with a new product's data in JSON format.
     * - Expects a 201 CREATED status in the response.
     * - Asserts that the response JSON contains the correct product name and description from `newProductData`.
     * <p>
     * 2. Deserializes the response JSON into a ProductDto object to verify the returned product details.
     * - Asserts that the returned product has a non-null ID, indicating it was successfully created.
     * <p>
     * 3. Retrieves the newly created product directly from the repository by its ID.
     * - Asserts that the product exists in the repository.
     * - Asserts that the name and description match the values from `newProductData`, ensuring the product was saved correctly.
     */
    @WithMockUser(roles = "COSMO_ADMIN")
    @Test
    void createProduct_shouldReturnCreatedProductDetails() throws Exception {
        // Step 1: Send a POST request to create a new product and verify the HTTP response and JSON content
        var response = mockMvc.perform(post(BASE_URL)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProductData)))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.name").value(newProductData.getName()),
                        jsonPath("$.description").value(newProductData.getDescription())
                )
                .andReturn();

        // Step 2: Deserialize the response JSON into a ProductDto object to verify that the product has been created
        ProductDto createdProduct = objectMapper.readValue(
                response.getResponse().getContentAsString(),
                ProductDto.class
        );

        // Assert that the created product has a non-null ID
        assertThat(createdProduct.getId()).isNotNull();

    // Step 3: Retrieve the product directly from the repository by filtering the Iterable and verify its properties
    var productFromRepo = StreamSupport.stream(productRepository.findAll().spliterator(), false)
            .filter(product -> product.getId().equals(createdProduct.getId()))
            .findFirst()
            .orElse(null);

        // Assert that the product exists in the repository and matches the createdProduct details
        assertThat(productFromRepo).isNotNull();
        assertThat(productFromRepo.getName()).isEqualTo(createdProduct.getName());
        assertThat(productFromRepo.getDescription()).isEqualTo(createdProduct.getDescription());
    }

    /**
     * Test updating an existing product.
     * Verifies that a PUT request can update a product's data and checks that
     * the updated product's name is changed as expected in the response and repository.
     */
    @WithMockUser(roles = "COSMO_ADMIN")
    @Test
    void updateExistingProduct_shouldReturnUpdatedProductDetails() throws Exception {
        // Retrieve a product to update
        var productToUpdate = StreamSupport.stream(productRepository.findAll().spliterator(), false)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No Product found! Impossible to complete the test."));

        // Create updated data for the product
        ProductCreationDto updatedData = newProductData.toBuilder()
                .name("Updated Comet Dust Cream")
                .build();

        // Perform the PUT request to update the product
        mockMvc.perform(put(BASE_URL + "/{id}", productToUpdate.getId())
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedData)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.name").value(updatedData.getName())
                );

        // Retrieve the updated product from the repository to verify the update
        var updatedProduct = productRepository.findById(productToUpdate.getId())
                .orElseThrow(() -> new NoSuchElementException("Updated Product not found in the repository."));

        // Assert that the product name has been updated
        assertThat(updatedProduct.getName()).isEqualTo("Updated Comet Dust Cream");
    }

    /**
     * Test updating a non-existent product.
     * Ensures that a PUT request to update a non-existent product
     * results in a 404 Not Found status with a relevant error message.
     */
    @Test
    @WithMockUser(roles = "COSMO_ADMIN")
    void updateProduct_whenProductDoesNotExist_shouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(put(BASE_URL + "/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(newProductData)))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.title").value("Product Not Found"),
                        jsonPath("$.status").value(404)
                );
    }

    /**
     * Test updating a product with an invalid price (non-positive).
     * Expects a 400 Bad Request status and a relevant validation error message.
     */
    @Test
    @WithMockUser
    void updateProductWithInvalidPrice_shouldReturnBadRequest() throws Exception {
        String productId = "24599e78-fb15-440f-af81-15822a42eb0f";
        String requestBody = """
        {
            "category": "HYGIENE",
            "name": "Intergalactic Soap",
            "description": "A soap that works in zero gravity.",
            "price": -99.99
        }
        """;

        mockMvc.perform(put("/api/v1/products/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem-type:validation-error"))
                .andExpect(jsonPath("$.title").value("Field Validation Exception"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Request validation failed"))
                .andExpect(jsonPath("$.invalidParams[0].fieldName").value("price"))
                .andExpect(jsonPath("$.invalidParams[0].reason").value("Price must be greater than 0"));
    }

    /**
     * Test updating a product with a missing categoryId.
     * Expects a 400 Bad Request status with a relevant validation error message.
     */
    @Test
    @WithMockUser
    void updateProductWithMissingCategoryId_shouldReturnBadRequest() throws Exception {
        String productId = "24599e78-fb15-440f-af81-15822a42eb0f";
        String requestBody = """
                {
                    "name": "Cosmic Oil",
                    "description": "Oil for cosmic travels",
                    "price": 49.99
                }
                """;

        mockMvc.perform(put("/api/v1/products/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem-type:validation-error"))
                .andExpect(jsonPath("$.title").value("Field Validation Exception"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Request validation failed"))
                .andExpect(jsonPath("$.invalidParams[0].fieldName").value("category"))
                .andExpect(jsonPath("$.invalidParams[0].reason").value("Product category cannot be null"));
    }

    /**
     * Test updating a product with a name that lacks the Cosmic-word.
     * Verifies that a product name must contain cosmic-related terms to meet validation requirements.
     * Ensures the response has a 400 Bad Request status and includes a validation error for the 'name' field.
     */
    @WithMockUser
    @Test
    void updateProductWithNoCosmicWordInTheProductName_shouldReturnBadRequest() throws Exception {
        String productId = "24599e78-fb15-440f-af81-15822a42eb0f";
        String requestBody = """
            {
                "category": "FOOD",
                "name": "Just A Regular Juice",
                "description": "A regular product description without cosmic terms.",
                "price": 39.99
            }
            """;

        mockMvc.perform(put("/api/v1/products/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem-type:validation-error"))
                .andExpect(jsonPath("$.title").value("Field Validation Exception"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Request validation failed"))
                .andExpect(jsonPath("$.instance").value("/api/v1/products/24599e78-fb15-440f-af81-15822a42eb0f"))
                .andExpect(jsonPath("$.invalidParams[0].fieldName").value("name"))
                .andExpect(jsonPath("$.invalidParams[0].reason").value(
                        org.hamcrest.Matchers.startsWith("The field must contain a cosmic term"))
                );
    }
}
