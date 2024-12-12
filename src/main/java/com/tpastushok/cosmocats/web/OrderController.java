package com.tpastushok.cosmocats.web;

import com.tpastushok.cosmocats.domain.OrderStatus;
import com.tpastushok.cosmocats.dto.order.OrderRequestDto;
import com.tpastushok.cosmocats.dto.order.OrderResponseDto;
import com.tpastushok.cosmocats.repository.persistence.projection.OrderProjection;
import com.tpastushok.cosmocats.repository.persistence.projection.PopularSoldProductsProjection;
import com.tpastushok.cosmocats.service.inerfaces.OrderService;
import com.tpastushok.cosmocats.web.mapper.OrderDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;
    private final OrderDtoMapper orderDtoMapper;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody @Valid OrderRequestDto orderRequestDto) {
        return ResponseEntity.ok(orderDtoMapper.toOrderResponse(orderService.placeOrder(orderRequestDto)));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        return ResponseEntity.ok(orderDtoMapper.toOrderResponse(orderService.getOrders()));
    }

    @GetMapping("/popular-ordered-products")
    public ResponseEntity<List<PopularSoldProductsProjection>> getPopularProducts() {
        return ResponseEntity.ok(orderService.getPopularProducts());
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrderProjection>> getOrderByCustomerEmail(@RequestParam String email) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerEmail(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderDtoMapper.toOrderResponse(orderService.getOrderByNaturalId(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        orderService.deleteByNaturalId(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable UUID id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderDtoMapper.toOrderResponse(orderService.updateStatusByNaturalId(id, status)));
    }
}
