package com.tpastushok.cosmocats.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum CustomerType {
    KITTY("kitty"), // Tiny chaos machines
    JUNIOR_CAT("junior-cat"), // Feline teens: cute but rebellious
    SENIOR_CAT("senior-cat"); // Mature meowsters of the universe

    private final String customerTypeName;

    CustomerType(String customerTypeName) {
        this.customerTypeName = customerTypeName;
    }
}
