package com.intern.orderservice.dto.payment;

import java.io.Serializable;
import java.math.BigDecimal;

public record CreatePaymentRequest(
        Long orderId,
        Long userId,
        BigDecimal paymentAmount
) implements Serializable {}
