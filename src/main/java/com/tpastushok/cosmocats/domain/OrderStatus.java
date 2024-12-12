package com.tpastushok.cosmocats.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum OrderStatus {
    PENDING("Pending approval or payment"),
    PROCESSING("Currently being processed"),
    SHIPPED("Shipped to the customer"),
    DELIVERED("Delivered successfully"),
    CANCELLED("Cancelled by the customer or system");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
