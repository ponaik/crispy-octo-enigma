package com.intern.orderservice.exception;

import com.intern.orderservice.model.enums.OrderStatus;
import org.springframework.security.access.AccessDeniedException;

public class StatusModificationIllegalAccessException extends AccessDeniedException {
    public StatusModificationIllegalAccessException(OrderStatus status) {
        super(String.format("User does not have permission to modify status to " + status.toString()));
    }
}
