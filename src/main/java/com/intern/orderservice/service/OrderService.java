package com.intern.orderservice.service;

import com.intern.orderservice.dto.CreateOrderRequest;
import com.intern.orderservice.dto.OrderUserResponse;
import com.intern.orderservice.dto.UpdateOrderStatusRequest;
import com.intern.orderservice.model.enums.OrderStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    Optional<OrderUserResponse> getOrderById(Long id);

    List<OrderUserResponse> getOrdersByIds(Collection<Long> ids);

    List<OrderUserResponse> getOrdersByStatuses(Collection<OrderStatus> statuses);

    void deleteOrderById(Long id);

    OrderUserResponse updateOrderStatusById(Long id, UpdateOrderStatusRequest request);

    OrderUserResponse createOrder(CreateOrderRequest request);
}
