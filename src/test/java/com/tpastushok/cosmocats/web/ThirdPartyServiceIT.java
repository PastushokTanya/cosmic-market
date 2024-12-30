package com.tpastushok.cosmocats.web;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.tpastushok.cosmocats.AbstractIt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URL;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.tpastushok.cosmocats.util.SecurityUtil.API_KEY_HEADER;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WireMockTest(httpPort = 8081) // WireMock listens on port 8081
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "application.competitors-price-observer.url-wildcard=http://localhost:8081/competitors-price-observer/v1/product/{productId}/prices"
})
/**
 * Integration tests for the third-party service price observer.
 * These tests verify communication with a simulated third-party service
 * using WireMock to stub responses for various scenarios.
 */
class ThirdPartyServiceIT extends AbstractIt {

    public static final String BEARER_TOKEN_STUB = "Bearer token stub";

    @Value("${application.competitors-price-observer.url-wildcard}")
    private String competitorsObserverUrlWildcard;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    JwtDecoder jwtDecoder;

    /**
     * Test retrieving prices from a third-party service.
     * Simulates a successful response from the third-party service and verifies
     * that the controller correctly processes and returns the response.
     */
    @WithMockUser(roles = "COSMO_MARKETOLOGIST")
    @Test
    void getCompetitorPrices_shouldReturnPricesSuccessfully() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();
        String thirdPartyServiceUrl = competitorsObserverUrlWildcard.replace("{productId}", productId.toString());
        String wiremockStubPath = new URL(thirdPartyServiceUrl).getPath();

        stubFor(get(urlPathEqualTo(wiremockStubPath))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "productUuid": "%s",
                                  "stores": [
                                    {"storeName": "Galaxy Mart", "regularPrice": 20.50, "markedDownPrice": 15.30},
                                    {"storeName": "Nebula Bazaar", "regularPrice": 22.75, "markedDownPrice": 18.99}
                                  ]
                                }
                                """.formatted(productId))));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}/competitor-price-observer", productId)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productUuid", is(productId.toString())))
                .andExpect(jsonPath("$.stores", hasSize(2)))
                .andExpect(jsonPath("$.stores[*].storeName", hasItem("Galaxy Mart")))
                .andExpect(jsonPath("$.stores[?(@.storeName == 'Galaxy Mart')].regularPrice", hasItem(20.50)))
                .andExpect(jsonPath("$.stores[?(@.storeName == 'Nebula Bazaar')].markedDownPrice", hasItem(18.99)));
    }

    /**
     * Test handling a Bad Gateway response from the third-party service.
     * Verifies that the controller returns a meaningful error message and status code
     * when the third-party service responds with a 502 Bad Gateway.
     */
    @WithMockUser(roles = "COSMO_MARKETOLOGIST")
    @Test
    void getCompetitorPrices_whenServiceReturnsBadGateway_shouldReturnInternalServerError() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();
        String thirdPartyServiceUrl = competitorsObserverUrlWildcard.replace("{productId}", productId.toString());
        String wiremockStubPath = new URL(thirdPartyServiceUrl).getPath();

        stubFor(get(urlPathEqualTo(wiremockStubPath))
                .willReturn(aResponse().withStatus(HttpStatus.BAD_GATEWAY.value())));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}/competitor-price-observer", productId)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type").value("third-party-service-error"))
                .andExpect(jsonPath("$.title").value("Error communicating with third-party service"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value(
                        "Third-party service error (HttpStatus: 500 INTERNAL_SERVER_ERROR): " +
                                "Unexpected error occurred while observing other store prices"
                ));
    }

    /**
     * Test handling an Internal Server Error from the third-party service.
     * Ensures the controller returns a relevant error response when the third-party
     * service responds with a 500 Internal Server Error.
     */
    @WithMockUser(roles = "COSMO_MARKETOLOGIST")
    @Test
    void getCompetitorPrices_whenServiceReturnsInternalServerError_shouldReturnInternalServerError() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();
        String thirdPartyServiceUrl = "/competitors-price-observer/v1/product/" + productId + "/prices";

        stubFor(get(urlPathEqualTo(thirdPartyServiceUrl))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}/competitor-price-observer", productId)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type").value("third-party-service-error"))
                .andExpect(jsonPath("$.title").value("Error communicating with third-party service"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value(
                        "Third-party service error (HttpStatus: 500 INTERNAL_SERVER_ERROR): " +
                                "Unexpected error occurred while observing other store prices"
                ));
    }

    /**
     * Test handling an empty stores list in the third-party service response.
     * Ensures that the controller correctly processes the response and returns
     * an empty list of stores.
     */
    @WithMockUser(roles = "COSMO_MARKETOLOGIST")
    @Test
    void getCompetitorPrices_whenServiceReturnsEmptyStores_shouldReturnEmptyList() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();

        stubFor(get(urlPathEqualTo("/competitors-price-observer/v1/product/" + productId + "/prices"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "productUuid": "%s",
                                  "stores": []
                                }
                                """.formatted(productId))));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}/competitor-price-observer", productId)
                        .header(API_KEY_HEADER, BEARER_TOKEN_STUB)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productUuid", is(productId.toString())))
                .andExpect(jsonPath("$.stores", hasSize(0))); // Verifying the empty stores list
    }
}
