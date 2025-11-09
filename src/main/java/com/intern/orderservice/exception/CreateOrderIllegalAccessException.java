package com.intern.orderservice.exception;

import com.intern.orderservice.dto.request.CreateOrderRequest;
import org.springframework.security.access.AccessDeniedException;

public class CreateOrderIllegalAccessException extends AccessDeniedException {
    public CreateOrderIllegalAccessException(CreateOrderRequest request) {
        super("CreateOrder requested without admin authority: " + request.toString());
    }
}
