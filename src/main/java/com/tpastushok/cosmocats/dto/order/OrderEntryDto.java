package com.tpastushok.cosmocats.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Value
@Builder
@Jacksonized
public class OrderEntryDto {
    @NotNull(message = "Product ID cannot be null")
    UUID productId;

    @Positive(message = "Quantity must be greater than 0")
    Long quantity;

    // price-field is used only when the OrderEntryDto is sent back to the client
    Double price;
}
