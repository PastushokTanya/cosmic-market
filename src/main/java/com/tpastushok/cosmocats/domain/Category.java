package com.tpastushok.cosmocats.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum Category {
    FOOD("Food for intergalactic consumption"),
    TOYS("Toys to entertain our feline overlords"),
    GADGETS("Space-age gadgets for every cat’s needs"),
    HYGIENE("Hygiene products suitable for space and zero gravity environments");

    private final String description;

    Category(String description) {
        this.description = description;
    }
}
