package com.intern.orderservice.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record ItemResponse(Long id, String name, BigDecimal price) implements Serializable {}
