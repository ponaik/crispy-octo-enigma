package com.intern.orderservice.dto;

import com.intern.orderservice.model.enums.OrderStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        OrderStatus status,
        LocalDateTime creationDate,
        List<OrderItemResponse> items
) implements Serializable {}
