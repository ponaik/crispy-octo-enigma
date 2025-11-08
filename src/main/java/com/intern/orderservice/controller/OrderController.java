package com.intern.orderservice.controller;


import com.intern.orderservice.dto.OrderUserResponse;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderUserResponse> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<OrderUserResponse>> getOrdersByIds(@RequestParam List<Long> ids) {
        List<OrderUserResponse> responses = orderService.getOrdersByIds(ids);
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/by-statuses")
    public ResponseEntity<List<OrderUserResponse>> getOrdersByStatuses(@RequestParam List<OrderStatus> statuses) {
        List<OrderUserResponse> responses = orderService.getOrdersByStatuses(statuses);
        return ResponseEntity.ok(responses);
    }
}

