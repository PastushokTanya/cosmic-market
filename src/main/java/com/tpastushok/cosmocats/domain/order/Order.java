package com.tpastushok.cosmocats.domain.order;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
@ToString
@EqualsAndHashCode
public class Order {
    UUID id;
    UUID customerId;
    List<OrderEntry> entries;
    Double totalPrice;
}
