package com.intern.orderservice.service;

import com.intern.orderservice.dto.CreateOrderRequest;
import com.intern.orderservice.dto.OrderResponse;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);
}
