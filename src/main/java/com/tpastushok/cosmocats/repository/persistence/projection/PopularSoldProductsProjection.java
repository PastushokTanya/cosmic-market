package com.tpastushok.cosmocats.repository.persistence.projection;

public interface PopularSoldProductsProjection {
    String getProductName();

    Long getNumberOfProducts();

    Double getTotalPrice();
}
