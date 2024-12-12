package com.tpastushok.cosmocats.web;

import com.tpastushok.cosmocats.AbstractIt;
import com.tpastushok.cosmocats.annotation.TurnFeatureToggleOff;
import com.tpastushok.cosmocats.annotation.TurnFeatureToggleOn;
import com.tpastushok.cosmocats.featuretoggle.FeatureToggleExtension;
import com.tpastushok.cosmocats.featuretoggle.FeatureToggleService;
import com.tpastushok.cosmocats.featuretoggle.FeatureToggles;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@DisplayName("Product Controller For Target Audience IT")
@AutoConfigureMockMvc
@ExtendWith(FeatureToggleExtension.class)
public class ProductControllerForTargetAudienceIT extends AbstractIt {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeatureToggleService featureToggleService;

    /**
     * Test that accessing the Kitty Products endpoint with the feature toggle OFF returns 404.
     * Uses custom `@TurnFeatureToggleOff` annotation.
     */
    @Test
    @SneakyThrows
    @TurnFeatureToggleOff(FeatureToggles.KITTY_PRODUCTS)
    void getKittyProducts_whenFeatureToggleIsOff_shouldReturnNotFound() {
        mockMvc.perform(get("/api/v1/products/kitty-products"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Feature is disabled"));
    }

    /**
     * Test that accessing the Kitty Products endpoint with the feature toggle ON returns 200.
     * Uses custom `@TurnFeatureToggleOn` annotation.
     */
    @Test
    @SneakyThrows
    @TurnFeatureToggleOn(FeatureToggles.KITTY_PRODUCTS)
    void getKittyProducts_whenFeatureToggleIsOn_shouldReturnOk() {
        mockMvc.perform(get("/api/v1/products/kitty-products"))
                .andExpect(status().isOk());
    }

    /**
     * Parameterized test for multiple feature toggles and endpoints.
     * Dynamically enables/disables feature toggles and verifies the response status.
     */
    @ParameterizedTest(name = "Feature: {0}, Toggle On: {1}, Endpoint: {2}, Expected Status: {3}")
    @MethodSource("productTestArguments")
    @SneakyThrows
    void testProductEndpoints_withDynamicFeatureToggleControl(
            FeatureToggles toggle,
            boolean toggleOn,
            String endpoint,
            int expectedStatus
    ) {

        // Dynamically enable or disable the feature toggle
        if (toggleOn) {
            featureToggleService.turnOn(toggle.getName());
        } else {
            featureToggleService.turnOff(toggle.getName());
        }

        // Perform the request and validate the response
        mockMvc.perform(get(endpoint))
                .andExpect(status().is(expectedStatus));
    }

    /**
     * Provides test cases for the parameterized test.
     * Each entry includes a feature toggle, its state, an endpoint, and the expected response status.
     */
    static Stream<Arguments> productTestArguments() {
        return Stream.of(
                arguments(FeatureToggles.KITTY_PRODUCTS, false, "/api/v1/products/kitty-products", 404),
                arguments(FeatureToggles.KITTY_PRODUCTS, true, "/api/v1/products/kitty-products", 200),
                arguments(FeatureToggles.JUNIOR_CAT_PRODUCTS, false, "/api/v1/products/junior-cat-products", 404),
                arguments(FeatureToggles.JUNIOR_CAT_PRODUCTS, true, "/api/v1/products/junior-cat-products", 200),
                arguments(FeatureToggles.SENIOR_CAT_PRODUCTS, false, "/api/v1/products/senior-cat-products", 404),
                arguments(FeatureToggles.SENIOR_CAT_PRODUCTS, true, "/api/v1/products/senior-cat-products", 200)
        );
    }
}
