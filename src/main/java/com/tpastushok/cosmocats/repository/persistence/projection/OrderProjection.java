package com.tpastushok.cosmocats.repository.persistence.projection;

public interface OrderProjection {
    String getOrderReference();

    Double getTotalPrice();

    String getStatus();
}
