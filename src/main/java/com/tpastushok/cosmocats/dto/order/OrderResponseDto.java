package com.tpastushok.cosmocats.dto.order;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class OrderResponseDto {
    String id;
    String customerName;
    String address;
    String email;
    double totalPrice;
    String status;
    List<OrderEntryDto> entries;
    LocalDateTime creationDate;
}
