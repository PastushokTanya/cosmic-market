package com.tpastushok.cosmocats.dto.product;

import com.tpastushok.cosmocats.dto.validation.CosmicWordCheck;
import com.tpastushok.cosmocats.dto.validation.ExtendedValidation;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(toBuilder = true)
@GroupSequence({ ProductCreationDto.class, ExtendedValidation.class})
public class ProductCreationDto {
    @NotNull(message = "Product categoryId cannot be null")
    UUID categoryId;

    @NotNull(message = "Product name cannot be null")
    @NotBlank(message = "Product name cannot be empty")
    @CosmicWordCheck // Enforcing that 'name' should contain a cosmic term
    String name;
    String description;

    @Positive(message = "Price must be greater than 0")
    Double price;
}
