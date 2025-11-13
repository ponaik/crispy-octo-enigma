package com.intern.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collection;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ItemsNotFoundException extends RuntimeException {
    public ItemsNotFoundException(Collection<Long> missingIds) {
        super("Items not found with IDs: " + missingIds);
    }
}