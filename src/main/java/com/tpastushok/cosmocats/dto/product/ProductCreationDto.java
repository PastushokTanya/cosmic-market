package com.tpastushok.cosmocats.dto.product;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ProductCreationDto {
    UUID categoryId;
    String name;
    String description;
    Double price;
}
