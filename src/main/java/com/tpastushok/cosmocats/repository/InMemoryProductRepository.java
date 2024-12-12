package com.tpastushok.cosmocats.repository;

import com.tpastushok.cosmocats.domain.Category;
import com.tpastushok.cosmocats.repository.persistence.entity.ProductEntity;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.tpastushok.cosmocats.domain.CustomerType.*;

@Repository
@Deprecated
public class InMemoryProductRepository implements ProductRepository {
    private final List<ProductEntity> products = mockProductsList();

    @Override
    public <S extends ProductEntity> S save(S entity) {
        // delete old version
        deleteById(entity.getId());

        // add new one
        products.add(entity);
        return entity;
    }

    @Override
    public <S extends ProductEntity> Iterable<S> saveAll(Iterable<S> entities) {
        // Convert Iterable<S> to a List<S> using streams
        List<S> entityList = StreamSupport.stream(entities.spliterator(), false).toList();

        // Add all entities to the existing products list
        products.addAll(entityList);

        // Return the saved entities
        return entityList;
    }

    @Override
    public Optional<ProductEntity> findById(UUID uuid) {
        return products.stream()
                .filter(product -> product.getId().equals(uuid))
                .findFirst();
    }

    @Override
    public boolean existsById(UUID uuid) {
        return products.stream()
                .anyMatch(product -> product.getId().equals(uuid));
    }

    @Override
    public Iterable<ProductEntity> findAll() {
        return new ArrayList<>(products); // Return a copy of the list
    }

    @Override
    public Iterable<ProductEntity> findAllById(Iterable<UUID> uuids) {

        Set<UUID> uuidSet = StreamSupport.stream(uuids.spliterator(), false)
                .collect(Collectors.toSet());

        return products.stream()
                .filter(product -> uuidSet.contains(product.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return products.size();
    }

    @Override
    public void deleteById(UUID id) {
        products.removeIf(product -> product.getId().equals(id));
    }

    @Override
    public void delete(ProductEntity entity) {
        products.removeIf(product -> product.equals(entity));
    }

    @Override
    public void deleteAllById(Iterable<? extends UUID> uuids) {

        Set<UUID> uuidSet = StreamSupport.stream(uuids.spliterator(), false)
                .collect(Collectors.toSet());

        products.removeIf(product -> uuidSet.contains(product.getId()));
    }

    @Override
    public void deleteAll(Iterable<? extends ProductEntity> entities) {

        Set<ProductEntity> entitySet = StreamSupport.stream(entities.spliterator(), false)
                .collect(Collectors.toSet());

        products.removeIf(entitySet::contains);
    }

    @Override
    public void deleteAll() {
        products.clear();
    }

    private List<ProductEntity> mockProductsList() {
        List<ProductEntity> result = List.of(
                ProductEntity.builder()
                        .id(UUID.fromString("77777777-0000-0000-0000-000000000001"))
                        .category(Category.TOYS)
                        .name("Anti-Gravity Yarn Ball")
                        .description("A yarn ball that floats in zero gravity, perfect for cosmic playtime.")
                        .price(49.99)
                        .targetAudience(KITTY)
                        .build(),

                ProductEntity.builder()
                        .id(UUID.fromString("77777777-0000-0000-0000-000000000002"))
                        .category(Category.FOOD)
                        .name("Cosmic Milk")
                        .description("A refreshing drink made from milk harvested from cosmic cows.")
                        .price(15.99)
                        .targetAudience(JUNIOR_CAT)
                        .build(),

                ProductEntity.builder()
                        .id(UUID.fromString("77777777-0000-0000-0000-000000000003"))
                        .category(Category.GADGETS)
                        .name("Stardust Blanket")
                        .description("A warm blanket infused with stardust for cozy nights in space.")
                        .price(99.99)
                        .targetAudience(SENIOR_CAT)
                        .build(),

                ProductEntity.builder()
                        .id(UUID.fromString("77777777-0000-0000-0000-000000000004"))
                        .category(Category.FOOD)
                        .name("Galaxy Catnip")
                        .description("Specially cultivated catnip that provides a euphoric space experience.")
                        .price(12.99)
                        .targetAudience(KITTY)
                        .build(),

                ProductEntity.builder()
                        .id(UUID.fromString("77777777-0000-0000-0000-000000000005"))
                        .category(Category.GADGETS)
                        .name("Nebula Scratching Post")
                        .description("A scratching post made from sturdy asteroid materials.")
                        .price(79.99)
                        .targetAudience(SENIOR_CAT)
                        .build()
        );

        //make result List mutable before its returning
        return new ArrayList<>(result);
    }
}
