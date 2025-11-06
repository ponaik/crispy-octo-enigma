package com.intern.orderservice.dto;

import com.intern.orderservice.model.enums.OrderStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record OrderUserResponse(
        Long id,
        UserResponse user,
        OrderStatus status,
        LocalDateTime creationDate,
        List<OrderItemResponse> items
) implements Serializable {}
