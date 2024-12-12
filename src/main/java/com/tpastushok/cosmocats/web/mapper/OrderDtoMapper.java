package com.tpastushok.cosmocats.web.mapper;

import com.tpastushok.cosmocats.domain.order.Order;
import com.tpastushok.cosmocats.dto.order.OrderResponseDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderDtoMapper {

    OrderResponseDto toOrderResponse(Order order);

    List<OrderResponseDto> toOrderResponse(List<Order> orders);
}
