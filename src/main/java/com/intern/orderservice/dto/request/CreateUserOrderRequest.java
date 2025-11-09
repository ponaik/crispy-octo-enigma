package com.intern.orderservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateUserOrderRequest(
        @NotEmpty(message = "items must not be empty")
        @Valid
        List<CreateOrderItemRequest> items
) {}