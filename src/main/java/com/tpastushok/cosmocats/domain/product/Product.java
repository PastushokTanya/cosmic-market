package com.tpastushok.cosmocats.domain.product;

import com.tpastushok.cosmocats.domain.Category;
import com.tpastushok.cosmocats.domain.CustomerType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(toBuilder = true) // Enable toBuilder for creating modified copies
@ToString

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// Only fields explicitly marked with @EqualsAndHashCode.Include are used for equality
public class Product {

    // Only the 'id' field will be used for equals() and hashCode()
    @EqualsAndHashCode.Include
    UUID id;
    Category category;
    String name;
    String description;
    Double price;
    CustomerType targetAudience;
}
