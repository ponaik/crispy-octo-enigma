package com.intern.orderservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "userId must be provided")
        Long userId,

        @NotEmpty(message = "items must not be empty")
        @Valid
        List<CreateOrderItemRequest> items
) {}