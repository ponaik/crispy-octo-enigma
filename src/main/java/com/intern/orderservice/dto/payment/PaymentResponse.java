package com.intern.orderservice.dto.payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String id,
        Long orderId,
        Long userId,
        PaymentStatus status,
        Instant timestamp,
        BigDecimal paymentAmount
)  implements Serializable {}
