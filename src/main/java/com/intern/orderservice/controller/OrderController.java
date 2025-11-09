package com.intern.orderservice.controller;


import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.request.CreateUserOrderRequest;
import com.intern.orderservice.dto.request.UpdateOrderStatusRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.service.OrderServiceAuthorizationDecorator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderServiceAuthorizationDecorator orderService;

    @Autowired
    public OrderController(OrderServiceAuthorizationDecorator orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderUserResponse> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/batch")
    public ResponseEntity<List<OrderUserResponse>> getOrdersByIds(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(orderService.getOrdersByIds(ids));
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<OrderUserResponse>> getOrdersByStatuses(@RequestParam List<OrderStatus> statuses) {
        return ResponseEntity.ok(orderService.getOrdersByStatuses(statuses));
    }

    @PostMapping("/admin")
    public ResponseEntity<OrderUserResponse> createOrder(@RequestBody @Valid CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @PostMapping
    public ResponseEntity<OrderUserResponse> createUserOrder(@RequestBody @Valid CreateUserOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderUserResponse> updateOrderStatusById(
            @PathVariable Long id,
            @RequestBody @Valid UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatusById(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderById(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }
}

