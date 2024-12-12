package com.tpastushok.cosmocats.domain.order;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@ToString
@EqualsAndHashCode
public class OrderEntry {
    UUID productId;
    Long quantity;
    Double price;
}
