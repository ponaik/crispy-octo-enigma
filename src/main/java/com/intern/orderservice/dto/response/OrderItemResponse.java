package com.intern.orderservice.dto.response;

import java.io.Serializable;

public record OrderItemResponse(Long id, ItemResponse item, Integer quantity) implements Serializable {}
