package com.intern.orderservice.exception;

import com.intern.orderservice.dto.payment.PaymentStatus;

public class UnknownPaymentStatusException extends RuntimeException {
    public UnknownPaymentStatusException(PaymentStatus status) {
        super("Unknown payment status: " + status);
    }
}
