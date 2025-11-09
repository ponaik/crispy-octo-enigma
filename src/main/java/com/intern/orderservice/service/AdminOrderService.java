package com.intern.orderservice.service;

import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.request.UpdateOrderStatusRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.model.enums.OrderStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AdminOrderService {
    Optional<OrderUserResponse> getOrderById(Long id);

    List<OrderUserResponse> getOrdersByIds(Collection<Long> ids);

    List<OrderUserResponse> getOrdersByStatuses(Collection<OrderStatus> statuses);

    OrderUserResponse createOrder(CreateOrderRequest request);

    OrderUserResponse updateOrderStatusById(Long id, UpdateOrderStatusRequest request);

    void deleteOrderById(Long id);
}
