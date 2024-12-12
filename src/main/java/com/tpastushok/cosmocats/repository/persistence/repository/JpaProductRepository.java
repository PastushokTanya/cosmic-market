package com.tpastushok.cosmocats.repository.persistence.repository;

import com.tpastushok.cosmocats.repository.ProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public interface JpaProductRepository extends ProductRepository {
}
