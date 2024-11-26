package com.tpastushok.cosmocats.featuretoggle;

import lombok.Getter;

@Getter
public enum FeatureToggles {
    KITTY_PRODUCTS("kitty-products"),
    JUNIOR_CAT_PRODUCTS("junior-cat-products"),
    SENIOR_CAT_PRODUCTS("senior-cat-products");

    private final String name;

    FeatureToggles(String name) {
        this.name = name;
    }
}
