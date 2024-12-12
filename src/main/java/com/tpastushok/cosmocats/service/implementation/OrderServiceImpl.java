package com.tpastushok.cosmocats.service.implementation;

import com.tpastushok.cosmocats.domain.OrderStatus;
import com.tpastushok.cosmocats.domain.order.Order;
import com.tpastushok.cosmocats.dto.order.OrderEntryDto;
import com.tpastushok.cosmocats.dto.order.OrderRequestDto;
import com.tpastushok.cosmocats.repository.ProductRepository;
import com.tpastushok.cosmocats.repository.persistence.entity.OrderEntity;
import com.tpastushok.cosmocats.repository.persistence.entity.OrderEntryEntity;
import com.tpastushok.cosmocats.repository.persistence.entity.ProductEntity;
import com.tpastushok.cosmocats.repository.persistence.projection.OrderProjection;
import com.tpastushok.cosmocats.repository.persistence.projection.PopularSoldProductsProjection;
import com.tpastushok.cosmocats.repository.persistence.repository.OrderRepository;
import com.tpastushok.cosmocats.service.exception.NoSuchOrderException;
import com.tpastushok.cosmocats.service.exception.NoSuchProductException;
import com.tpastushok.cosmocats.service.exception.PersistenceException;
import com.tpastushok.cosmocats.service.inerfaces.OrderService;
import com.tpastushok.cosmocats.web.mapper.OrderEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderEntityMapper orderEntityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        return orderEntityMapper.toOrders(orderRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByNaturalId(UUID id) {
        Optional<OrderEntity> result = orderRepository.findByNaturalId(id);

        if (result.isEmpty()) {
            log.error("There is no order with NaturalId: {}", id);
            throw new NoSuchOrderException(id.toString());
        } else {
            log.info("Order retrieved successfully by NaturalId: {}", id);
            return orderEntityMapper.toOrder(result.get());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderProjection> getOrdersByCustomerEmail(String customerEmail) {
        List<OrderProjection> result = orderRepository.findDistinctByEmailOrderByStatus(customerEmail);

        log.info("Retrieved {} orders where customerEmail is: {}", result.size(), customerEmail);
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public Order placeOrder(OrderRequestDto orderRequestDto) {
        try {
            // Calculate totalPrice and create the list of OrderEntryEntity
            List<OrderEntryEntity> orderEntryEntities = orderRequestDto.getOrderEntries().stream()
                    .map(entry -> createOrderEntry(entry))
                    .collect(Collectors.toList());

            Double totalPrice = orderEntryEntities.stream()
                    .mapToDouble(OrderEntryEntity::getPrice)
                    .sum();

            // Build OrderEntity
            OrderEntity order = OrderEntity.builder()
                    .entries(orderEntryEntities)
                    .status(OrderStatus.PENDING)
                    .orderReference(UUID.randomUUID())
                    .email(orderRequestDto.getEmail())
                    .customerName(orderRequestDto.getCustomerName())
                    .address(orderRequestDto.getAddress())
                    .totalPrice(totalPrice)
                    .build();

            // Set order reference for each OrderEntryEntity
            orderEntryEntities.forEach(orderItem -> orderItem.setOrder(order));

            // Save the order and map to Order
            Order result = orderEntityMapper.toOrder(orderRepository.save(order));
            log.info("Successfully placed order: {}", result);
            return result;
        } catch (Throwable t) {
            log.error("Failed to place order. Request: {}", orderRequestDto, t);
            throw t;
        }
    }

    // Create OrderEntryEntity from OrderEntryDto
    private OrderEntryEntity createOrderEntry(OrderEntryDto entry) {
        ProductEntity product = productRepository.findById(entry.getProductId())
                .orElseThrow(() -> new NoSuchProductException("Product with ID: " + entry.getProductId() + " not found!"));

        Double price = entry.getQuantity() * product.getPrice();

        return OrderEntryEntity.builder()
                .product(product)
                .quantity(entry.getQuantity())
                .price(price)
                .build();
    }

    @Override
    @Transactional
    public Order updateStatusByNaturalId(UUID id, OrderStatus newStatus) {
        try {
            OrderEntity order = orderRepository.findByNaturalId(id)
                    .orElseThrow(() -> new NoSuchOrderException(id.toString()));

            order.setStatus(newStatus);

            log.info("Successfully updated order with NaturalID: {} to status: {}", id, newStatus);
            return orderEntityMapper.toOrder(order);
        } catch (Throwable t) {
            log.error("Failed to change status of order with NaturalID: {} to {}", id, newStatus, t);
            throw new PersistenceException(t);
        }
    }

    @Override
    @Transactional
    public void deleteByNaturalId(UUID id) {
        try {
            orderRepository.deleteByNaturalId(id);
        } catch (Throwable t) {
            log.error("Failed to delete order with NaturalID: {}", id, t);
            throw new PersistenceException(t);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopularSoldProductsProjection> getPopularProducts() {
        List<PopularSoldProductsProjection> result = orderRepository.getPopularProducts();

        log.info("Successfully retrieved {} projections of the most popular products", result.size());
        return result;
    }
}
