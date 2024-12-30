package com.tpastushok.cosmocats.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.tpastushok.cosmocats.AbstractIt;
import com.tpastushok.cosmocats.domain.product.Product;
import com.tpastushok.cosmocats.dto.order.OrderEntryDto;
import com.tpastushok.cosmocats.dto.order.OrderRequestDto;
import com.tpastushok.cosmocats.dto.product.ProductCreationDto;
import com.tpastushok.cosmocats.repository.ProductRepository;
import com.tpastushok.cosmocats.repository.persistence.repository.OrderRepository;
import com.tpastushok.cosmocats.service.inerfaces.OrderService;
import com.tpastushok.cosmocats.service.inerfaces.ProductService;
import com.tpastushok.cosmocats.web.mapper.ProductDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.tpastushok.cosmocats.domain.Category.*;
import static com.tpastushok.cosmocats.domain.CustomerType.*;
import static com.tpastushok.cosmocats.util.SecurityUtil.API_KEY_HEADER;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
public class OrderControllerIT extends AbstractIt {
    private static final String BASE_URL = "/api/v1/orders";

    public static final String BEARER_TOKEN_STUB = "Bearer token stub";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductDtoMapper productDtoMapper;

    @SpyBean
    private ProductService productService;

    @SpyBean
    private OrderService orderService;

    @MockBean
    JwtDecoder jwtDecoder;

    @BeforeEach
    void setup() {
        Mockito.reset(productService);
        Mockito.reset(orderService);

        productRepository.deleteAll();
        orderRepository.deleteAll();

        saveSampleProducts();

        Jwt jwtMock = Jwt.withTokenValue("dummy-token")
                .header("alg", "none")
                .claim("access", "ProductApi")
                .claim("authorities", List.of())
                .build();
        when(jwtDecoder.decode(anyString())).thenReturn(jwtMock);
    }

    /**
     * Test for creating an order and verifying it in the repository and through the controller.
     * This test ensures that:
     * - The order is correctly created in the repository.
     * - The order is returned through the controller with the correct entries and product details.
     * - The response contains the correct number of order entries and that each entry matches the order request.
     */
    @Test
    @WithMockUser(roles = "COSMO_ADMIN")
    void shouldCreateOrderInRepoAndReturnItViaController() throws Exception {

        // Verify no orders before the request
        assert orderRepository.findAll().stream().toList().isEmpty();

        // Place new order and check that we have 2 entries in the response
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getOrderRequestDto()))
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries").isArray())  // Change "orderEntries" to "entries"
                .andExpect(jsonPath("$.entries.length()").value(2));  // Ensure 2 entries in the response

        // Verify the order exists in the system
        assert orderRepository.findAll().size() == 1;  // Ensure that exactly 1 order was created

        // Get all the orders and verify the response contains 1 order with 2 entries
        mockMvc.perform(get(BASE_URL)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))  // Ensure we have 1 order in the response
                .andExpect(jsonPath("$[0].entries").isArray())  // Ensure entries exist in the response
                .andExpect(jsonPath("$[0].entries.length()").value(2))  // Ensure 2 entries in the order
                .andExpect(jsonPath("$[0].entries[0].productId").value(getOrderRequestDto().getOrderEntries().get(0).getProductId().toString()))  // Verify first product ID
                .andExpect(jsonPath("$[0].entries[0].quantity").value(getOrderRequestDto().getOrderEntries().get(0).getQuantity()))  // Verify first product quantity
                .andExpect(jsonPath("$[0].entries[1].productId").value(getOrderRequestDto().getOrderEntries().get(1).getProductId().toString()))  // Verify second product ID
                .andExpect(jsonPath("$[0].entries[1].quantity").value(getOrderRequestDto().getOrderEntries().get(1).getQuantity()));  // Verify second product quantity
    }

    @Test
    @WithMockUser(roles = "COSMO_ADMIN")
    void shouldReturnErrorForNonExistentProduct() throws Exception {
        // UUID for a non-existent product
        UUID nonExistentProductId = UUID.fromString("77777777-0000-0000-0000-000000000001");

        // Prepare the order request DTO with the non-existent product ID
        OrderRequestDto orderRequestDto = OrderRequestDto.builder()
                .address("Nebula Station, Sector 47B")
                .email("captain@starfleet.com")
                .customerName("Captain Nova Starfire")
                .orderEntries(Arrays.asList(
                        OrderEntryDto.builder()
                                .productId(nonExistentProductId)  // Use non-existent product ID
                                .quantity(1L)
                                .build()))
                .build();

        // Perform the POST request and verify the 404 error with the appropriate message
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDto))
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isNotFound())  // Verify 404 status
                .andExpect(jsonPath("$.title").value("Product Not Found"))  // Check the error title
                .andExpect(jsonPath("$.status").value(404))  // Ensure status code is 404
                .andExpect(jsonPath("$.detail").value("Product with ID: 77777777-0000-0000-0000-000000000001 not found!"))  // Check the detail message
                .andExpect(jsonPath("$.instance").value("/api/v1/orders"));  // Ensure the instance URL is correct
    }

    /**
     * Test for retrieving popular ordered products as a Projection
     * This test ensures that:
     * - When an order is placed with two products, the `/popular-ordered-products` endpoint returns the correct
     * product details (name, total price, and quantity) for those products.
     */
    @Test
    @WithMockUser(roles = "COSMO_BOSS")
    void shouldReturnPopularOrderedProductsWithCorrectDetails() throws Exception {
        // Place an order using the existing method
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getOrderRequestDto()))
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk());

        // Validate popular ordered products
        mockMvc.perform(get(BASE_URL + "/popular-ordered-products")
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())  // Ensure the response is an array
                .andExpect(jsonPath("$.length()").value(2))  // Ensure there are 2 products in the response
                .andExpect(jsonPath("$[0].numberOfProducts").value(getOrderRequestDto().getOrderEntries().get(1).getQuantity()))  // Verify quantity of second product
                .andExpect(jsonPath("$[1].numberOfProducts").value(getOrderRequestDto().getOrderEntries().get(0).getQuantity()))  // Verify quantity of first product
                .andExpect(jsonPath("$[0].productName").exists())  // Ensure first product name exists
                .andExpect(jsonPath("$[1].productName").exists())  // Ensure second product name exists
                .andExpect(jsonPath("$[0].totalPrice").exists())  // Verify total price exists for first product
                .andExpect(jsonPath("$[1].totalPrice").exists());  // Verify total price exists for second product
    }

    /**
     * Test retrieving orders by customer email and validating projection fields.
     * This test ensures that:
     * - Two orders are registered for different customer emails.
     * - Using the `/search` endpoint with one email returns the correct order projection.
     * - The projection fields (orderReference, totalPrice, status) are validated.
     */
    @Test
    @WithMockUser(roles = "COSMO_ADMIN")
    void getOrdersByCustomerEmail_shouldReturnOrderProjections() throws Exception {
        // Get existing products from the repository
        List<Product> existingProducts = productService.getProducts();

        // Ensure there are at least four products in the repository to use them for 2 customer orders
        assert existingProducts.size() >= 4;

        // First order request body using existing products
        String firstOrderRequestBody = objectMapper.writeValueAsString(OrderRequestDto.builder()
                .address("Cat Station, Galaxy Edge")
                .email("cat_owner1@cosmocats.com")
                .customerName("Cat Owner One")
                .orderEntries(
                        Arrays.asList(
                                OrderEntryDto.builder()
                                        .productId(existingProducts.get(0).getId())
                                        .quantity(2L)
                                        .build(),
                                OrderEntryDto.builder()
                                        .productId(existingProducts.get(1).getId())
                                        .quantity(3L)
                                        .build()
                        )
                )
                .build());

        // Second order request body using existing products
        String secondOrderRequestBody = objectMapper.writeValueAsString(OrderRequestDto.builder()
                .address("Cat Plaza, Universe Central")
                .email("cat_owner2@cosmocats.com")
                .customerName("Cat Owner Two")
                .orderEntries(
                        Arrays.asList(
                                OrderEntryDto.builder()
                                        .productId(existingProducts.get(2).getId())
                                        .quantity(1L)
                                        .build(),
                                OrderEntryDto.builder()
                                        .productId(existingProducts.get(3).getId())
                                        .quantity(4L)
                                        .build()
                        )
                )
                .build());

        // Register the first order
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstOrderRequestBody)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk());

        // Register the second order
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondOrderRequestBody)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk());

        // Search for orders by the first email
        String emailToSearch = "cat_owner1@cosmocats.com";
        mockMvc.perform(get(BASE_URL + "/search")
                        .param("email", emailToSearch)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())  // Ensure the response is an array
                .andExpect(jsonPath("$.length()").value(1))  // Ensure only one order projection is returned
                .andExpect(jsonPath("$[0].orderReference").isNotEmpty())  // Validate orderReference is present
                .andExpect(jsonPath("$[0].totalPrice").value(org.hamcrest.Matchers.closeTo(147.95, 0.01)))  // Validate totalPrice (example value)
                .andExpect(jsonPath("$[0].status").value("PENDING"));  // Validate status is PENDING
    }

    /**
     * Test for registering a new order, verifying initial status, updating the status,
     * and confirming the updated status via API endpoints.
     * This test ensures:
     * - A new order is created with `PENDING` status.
     * - The status is updated to `PROCESSING`.
     * - The updated status is correctly reflected when fetching the order.
     */
    @Test
    @WithMockUser(roles = "COSMO_ADMIN")
    void registerOrderAndUpdateStatus_shouldReflectUpdatedStatus() throws Exception {
        // Get existing products from the repository
        List<Product> existingProducts = productService.getProducts();

        // Ensure there are at least two products in the repository
        assert existingProducts.size() >= 2;

        // Create a new order request using existing product IDs
        String orderRequestBody = objectMapper.writeValueAsString(OrderRequestDto.builder()
                .address("Galactic Center, Andromeda")
                .email("cat_explorer@cosmocats.com")
                .customerName("Galactic Explorer")
                .orderEntries(
                        Arrays.asList(
                                OrderEntryDto.builder()
                                        .productId(existingProducts.get(0).getId())
                                        .quantity(3L)
                                        .build(),
                                OrderEntryDto.builder()
                                        .productId(existingProducts.get(1).getId())
                                        .quantity(2L)
                                        .build()
                        )
                )
                .build());

        // Register a new order and capture the returned ID
        String responseContent = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestBody)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the order ID from the response
        UUID orderId = UUID.fromString(JsonPath.parse(responseContent).read("$.id"));

        // Retrieve the order and verify its initial status is PENDING
        mockMvc.perform(get(BASE_URL + "/{id}", orderId)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Update the order status to PROCESSING
        mockMvc.perform(put(BASE_URL + "/{id}", orderId)
                        .param("status", "PROCESSING")
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));

        // Retrieve the order again and verify the updated status
        mockMvc.perform(get(BASE_URL + "/{id}", orderId)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    /**
     * Test for creating and deleting an order, verifying repository updates.
     * This test ensures that:
     * - An order is created in the repository and correctly retrieved.
     * - The order can be deleted using its ID.
     * - After deletion, no orders remain in the repository.
     */
    @Test
    @WithMockUser(roles = "COSMO_ADMIN")
    void shouldCreateAndDeleteOrderById() throws Exception {
        // Verify no orders exist before the test
        assert orderRepository.findAll().stream().toList().isEmpty();

        // Create a new order
        String orderRequestBody = objectMapper.writeValueAsString(getOrderRequestDto());

        // Register a new order and capture the returned ID
        String responseContent = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestBody)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the order ID from the response
        UUID orderId = UUID.fromString(JsonPath.parse(responseContent).read("$.id"));

        // Verify the order exists in the repository
        assert orderRepository.findByNaturalId(orderId).isPresent();

        // Delete the order by ID
        mockMvc.perform(delete(BASE_URL + "/{id}", orderId)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify the order is no longer in the repository
        assert orderRepository.findByNaturalId(orderId).isEmpty();

        // Ensure no orders exist in the system
        mockMvc.perform(get(BASE_URL)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf())) //Move this token to constants
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)); // Ensure there are no orders in the response
    }

    /**
     * Test for attempting to delete a non-existent order.
     * This test ensures that:
     * - When trying to delete an order that does not exist,
     *   the system returns a 204 No Content response.
     */
    @Test
    @WithMockUser(roles = "TRUSTED_CUSTOMER")
    void deleteNonExistentOrder_shouldReturnNoContentResponse() throws Exception {
        // UUID for a non-existent order
        UUID nonExistentOrderId = UUID.fromString("77777777-0000-0000-0000-000000000001");

        // Attempt to delete the non-existent order by ID
        mockMvc.perform(delete(BASE_URL + "/{id}", nonExistentOrderId)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    private void saveSampleProducts() {
        List.of(
                ProductCreationDto.builder()
                        .category(TOYS.name())
                        .name("Anti-Gravity Yarn Ball")
                        .description("A yarn ball that floats in zero gravity, perfect for cosmic playtime.")
                        .price(49.99)
                        .targetAudience(KITTY)
                        .build(),

                ProductCreationDto.builder()
                        .category(FOOD.name())
                        .name("Cosmic Milk")
                        .description("A refreshing drink made from milk harvested from cosmic cows.")
                        .price(15.99)
                        .targetAudience(JUNIOR_CAT)
                        .build(),

                ProductCreationDto.builder()
                        .category(GADGETS.name())
                        .name("Stardust Blanket")
                        .description("A warm blanket infused with stardust for cozy nights in space.")
                        .price(99.99)
                        .targetAudience(SENIOR_CAT)
                        .build(),

                ProductCreationDto.builder()
                        .category(FOOD.name())
                        .name("Galaxy Catnip")
                        .description("Specially cultivated catnip that provides a euphoric space experience.")
                        .price(12.99)
                        .targetAudience(KITTY)
                        .build(),

                ProductCreationDto.builder()
                        .category(GADGETS.name())
                        .name("Nebula Scratching Post")
                        .description("A scratching post made from sturdy asteroid materials.")
                        .price(79.99)
                        .targetAudience(SENIOR_CAT)
                        .build()
        ).forEach(p -> productService.createProduct(productDtoMapper.toProduct(p)));
    }

    private OrderRequestDto getOrderRequestDto() {
        List<Product> existedProducts = productService.getProducts();

        return OrderRequestDto.builder()
                .address("Nebula Station, Sector 47B")
                .email("captain@starfleet.com")
                .customerName("Captain Nova Starfire")
                .orderEntries(
                        Arrays.asList(
                                OrderEntryDto.builder()
                                        .productId(existedProducts.get(0).getId()) // first product ID from the repo
                                        .quantity(3L)
                                        .build(),
                                OrderEntryDto.builder()
                                        .productId(existedProducts.get(1).getId()) // second product ID from the repo
                                        .quantity(5L)
                                        .build()
                        )
                )
                .build();
    }

    /**
     * Test for validating order creation with invalid product quantity.
     * This test ensures that:
     * - Submitting an order with a product quantity of 0 fails validation.
     * - The response contains proper validation error details.
     */
    @Test
    @WithMockUser(roles = "TRUSTED_CUSTOMER")
    void createOrderWithZeroQuantity_shouldFailValidation() throws Exception {
        // Fetch an existing product ID
        UUID existingProductId = productService.getProducts().get(0).getId();

        // Request body with invalid quantity
        String invalidRequestBody = """
        {
            "orderEntries": [
                {
                    "productId": "%s",
                    "quantity": 0
                }
            ],
            "address": "Cosmic address",
            "email": "super_cat2@cat.com",
            "customerName": "Mr Cat Smith"
        }
    """.formatted(existingProductId);

        // Perform the request and validate the response
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem-type:validation-error"))
                .andExpect(jsonPath("$.title").value("Field Validation Exception"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Request validation failed"))
                .andExpect(jsonPath("$.invalidParams[0].fieldName").value("orderEntries[0].quantity"))
                .andExpect(jsonPath("$.invalidParams[0].reason").value("Quantity must be greater than 0"));
    }

    /**
     * Test for verifying that a user with the `TRUSTED_CUSTOMER` role can successfully create an order.
     * This test ensures:
     * - The order is placed successfully with a 200 OK response.
     * - The response contains an array of order entries with the correct number of items.
     */
    @Test
    @WithMockUser(roles = "TRUSTED_CUSTOMER")
    void createOrder_asTrustedCustomer_shouldReturnSuccessfulResponse() throws Exception {

        // Place new order and check that we have 2 entries in the response
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getOrderRequestDto()))
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk())  // Verify 200 OK status
                .andExpect(jsonPath("$.entries").isArray())  // Validate "entries" array exists
                .andExpect(jsonPath("$.entries.length()").value(2));  // Ensure 2 entries in the response
    }

    /**
     * Test for ensuring a user with the role `COSMO_MARKETOLOGIST` cannot create an order.
     * This test verifies that:
     * - The system denies access with a 403 Forbidden response.
     * - The error response contains the correct structure and fields.
     */
    @Test
    @WithMockUser(roles = "COSMO_MARKETOLOGIST")
    void createOrder_withMarketologistRole_shouldReturnForbiddenResponse() throws Exception {


        // Attempt to place a new order
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getOrderRequestDto()))
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isForbidden())  // Verify 403 Forbidden status
                .andExpect(jsonPath("$.type").value("authorization-denied"))  // Validate response type
                .andExpect(jsonPath("$.title").value("Forbidden"))  // Validate title field
                .andExpect(jsonPath("$.status").value(403))  // Validate status code
                .andExpect(jsonPath("$.detail").value("Access Denied"))  // Validate detail message
                .andExpect(jsonPath("$.instance").value(BASE_URL));  // Validate instance URI
    }

    /**
     * Test for ensuring an unauthorized user cannot create an order.
     * This test verifies that:
     * - The system denies access with a 401 Unauthorized response.
     * - The error response contains the correct headers and status code.
     */
    @Test
    void createOrder_asUnauthorizedUser_shouldReturnUnauthorizedResponse() throws Exception {

        // Attempt to place a new order
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getOrderRequestDto()))
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());  // Verify 401 Unauthorized status
    }
}
