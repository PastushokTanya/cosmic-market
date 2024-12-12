package com.tpastushok.cosmocats.web.mapper;

import com.tpastushok.cosmocats.domain.order.Order;
import com.tpastushok.cosmocats.domain.order.OrderEntry;
import com.tpastushok.cosmocats.repository.persistence.entity.OrderEntity;
import com.tpastushok.cosmocats.repository.persistence.entity.OrderEntryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.StreamSupport;

@Mapper(componentModel = "spring")
public interface OrderEntityMapper {

    @Mapping(target = "id", source = "orderReference")
    @Mapping(target = "entries", source = "entries", qualifiedByName = "toOrderEntry")
    Order toOrder(OrderEntity order);

    @Named("toOrderEntry")
    default OrderEntry toOrderEntry(OrderEntryEntity entity) {
        return OrderEntry.builder()
                .productId(entity.getProduct().getId())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .build();
    }

    default List<Order> toOrders(Iterable<OrderEntity> orderEntities) {
        return StreamSupport.stream(orderEntities.spliterator(), false)
                .map(this::toOrder)
                .toList();
    }
}
