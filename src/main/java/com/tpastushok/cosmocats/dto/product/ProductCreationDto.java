package com.tpastushok.cosmocats.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ProductCreationDto {
    @NotNull(message = "Product categoryId cannot be null")
    UUID categoryId;

    @NotNull(message = "Product name cannot be null")
    @NotBlank(message = "Product name cannot be empty")
    String name;
    String description;

    @Positive(message = "Price must be greater than 0")
    Double price;
}
