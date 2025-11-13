package com.intern.orderservice.dto.request;

import com.intern.orderservice.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull
        OrderStatus status
) {}
