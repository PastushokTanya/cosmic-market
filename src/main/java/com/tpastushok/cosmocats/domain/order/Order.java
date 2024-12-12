package com.tpastushok.cosmocats.domain.order;

import com.tpastushok.cosmocats.domain.OrderStatus;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder
@ToString
@EqualsAndHashCode
public class Order {
    UUID id;
    String customerName;
    String address;
    String email;
    List<OrderEntry> entries;
    Double totalPrice;
    OrderStatus status;
    LocalDateTime creationDate;
}
