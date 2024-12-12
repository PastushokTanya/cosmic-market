package com.tpastushok.cosmocats.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface NaturalIdRepository<T, ID> extends JpaRepository<T, ID> {

    Optional<T> findByNaturalId(ID naturalId);

    void deleteByNaturalId(ID naturalId);
}
