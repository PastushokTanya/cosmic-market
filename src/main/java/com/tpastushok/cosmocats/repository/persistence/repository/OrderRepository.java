package com.tpastushok.cosmocats.repository.persistence.repository;

import com.tpastushok.cosmocats.repository.persistence.NaturalIdRepository;
import com.tpastushok.cosmocats.repository.persistence.entity.OrderEntity;
import com.tpastushok.cosmocats.repository.persistence.projection.OrderProjection;
import com.tpastushok.cosmocats.repository.persistence.projection.PopularSoldProductsProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends NaturalIdRepository<OrderEntity, UUID> {

    List<OrderProjection> findDistinctByEmailOrderByStatus(String email);

    @Query("""
           SELECT p.name            AS productName,
                  SUM(oe.quantity)  AS numberOfProducts,
                  SUM(o.totalPrice) AS totalPrice
           FROM OrderEntryEntity oe
           JOIN oe.product p
           JOIN oe.order o
           GROUP BY p.name
           ORDER BY numberOfProducts DESC
           LIMIT 10
           """)
    List<PopularSoldProductsProjection> getPopularProducts();
}
