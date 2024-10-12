package com.tpastushok.cosmocats.domain.product;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@ToString

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// Only fields explicitly marked with @EqualsAndHashCode.Include are used for equality
public class Product {

    // Only the 'id' field will be used for equals() and hashCode()
    @EqualsAndHashCode.Include
    UUID id;
    UUID categoryId;
    String name;
    String description;
    Double price;
}
