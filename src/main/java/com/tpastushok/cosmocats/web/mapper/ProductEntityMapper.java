package com.tpastushok.cosmocats.web.mapper;

import com.tpastushok.cosmocats.domain.product.Product;
import com.tpastushok.cosmocats.repository.persistence.entity.ProductEntity;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Mapper(componentModel = "spring", imports = {UUID.class})
public interface ProductEntityMapper {

    ProductEntity toProductEntity(Product product);

    Product toProduct(ProductEntity productEntity);

    List<ProductEntity> toProductEntities(List<Product> products);

    default List<Product> toProducts(Iterable<ProductEntity> productEntities) {
        return StreamSupport.stream(productEntities.spliterator(), false)
                .map(this::toProduct)
                .toList();
    }
}