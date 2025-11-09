package com.intern.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderItemRequest(
        @NotNull(message = "itemId must be provided")
        Long itemId,

        @NotNull(message = "quantity must be provided")
        @Positive(message = "quantity must be greater than zero")
        Integer quantity
) {}

