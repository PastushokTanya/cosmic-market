package com.tpastushok.cosmocats.service.inerfaces;

import com.tpastushok.cosmocats.domain.OrderStatus;
import com.tpastushok.cosmocats.domain.order.Order;
import com.tpastushok.cosmocats.dto.order.OrderRequestDto;
import com.tpastushok.cosmocats.repository.persistence.projection.OrderProjection;
import com.tpastushok.cosmocats.repository.persistence.projection.PopularSoldProductsProjection;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    List<Order> getOrders();

    Order getOrderByNaturalId(UUID id);

    List<OrderProjection> getOrdersByCustomerEmail(String customerEmail);

    Order placeOrder(OrderRequestDto orderRequestDto);

    Order updateStatusByNaturalId(UUID orderId, OrderStatus status);

    void deleteByNaturalId(UUID id);

    List<PopularSoldProductsProjection> getPopularProducts();
}
