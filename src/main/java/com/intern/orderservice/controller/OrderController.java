package com.intern.orderservice.controller;

import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.request.UpdateOrderStatusRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.service.OrderServiceFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order Management API")
public class OrderController {

    private final OrderServiceFacade orderService;

    @Autowired
    public OrderController(OrderServiceFacade orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves details for a specific order. Users see their own orders, while admins can access any order.")
    public ResponseEntity<OrderUserResponse> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/batch")
    @Operation(summary = "Get orders in batch", description = "Retrieves a collection of orders matching a list of provided IDs.")
    public ResponseEntity<List<OrderUserResponse>> getOrdersByIds(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(orderService.getOrdersByIds(ids));
    }

    @GetMapping("/status")
    @Operation(summary = "Get orders by status", description = "Retrieves a list of orders filtered by one or more statuses (e.g., NEW, PAID, SHIPPED).")
    public ResponseEntity<List<OrderUserResponse>> getOrdersByStatuses(@RequestParam List<OrderStatus> statuses) {
        return ResponseEntity.ok(orderService.getOrdersByStatuses(statuses));
    }

    @PostMapping
    @Operation(summary = "Create an order", description = "Places a new order. For standard users, the userId is automatically set to their own ID; admins can specify a userId manually.")
    public ResponseEntity<OrderUserResponse> createOrder(@RequestBody @Valid CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update order status", description = "Updates an order's status. Standard users are restricted to cancelling their own orders (CANCELLED), whereas admins can set any valid status.")
    public ResponseEntity<OrderUserResponse> updateOrderStatusById(
            @PathVariable Long id,
            @RequestBody @Valid UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatusById(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an order", description = "Deletes an order record. Users can only delete their own orders, while admins have full deletion authority.")
    public ResponseEntity<Void> deleteOrderById(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }
}