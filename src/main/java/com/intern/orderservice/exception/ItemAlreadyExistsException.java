package com.intern.orderservice.exception;

import java.math.BigDecimal;

public class ItemAlreadyExistsException extends RuntimeException {
    public ItemAlreadyExistsException(String name, BigDecimal price) {
        super("Item with name " + name + " and price " + price + " already exists");
    }
}
