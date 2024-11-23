package com.tpastushok.cosmocats.domain.order;

import com.tpastushok.cosmocats.domain.product.Product;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@Builder
@ToString
@EqualsAndHashCode
public class OrderEntry {
    Product product;
    Long quantity;
}
