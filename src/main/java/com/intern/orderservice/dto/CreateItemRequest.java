package com.intern.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateItemRequest(
        @NotBlank(message = "name must not be blank")
        String name,

        @NotNull(message = "price must be provided")
        @Positive(message = "price must be positive")
        BigDecimal price
) {}
