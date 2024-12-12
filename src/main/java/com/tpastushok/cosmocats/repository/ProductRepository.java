package com.tpastushok.cosmocats.repository;

import com.tpastushok.cosmocats.repository.persistence.entity.ProductEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ProductRepository extends CrudRepository<ProductEntity, UUID> {
}
