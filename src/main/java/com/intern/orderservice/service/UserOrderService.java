package com.intern.orderservice.service;

import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.request.UpdateOrderStatusRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.model.enums.OrderStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserOrderService {
    Optional<OrderUserResponse> getUserOrderById(Long id, String email);

    List<OrderUserResponse> getUserOrdersByIds(Collection<Long> ids, String email);

    List<OrderUserResponse> getUserOrdersByStatuses(Collection<OrderStatus> statuses, String email);

    OrderUserResponse createUserOrder(CreateOrderRequest request, String email);

    OrderUserResponse updateUserOrderStatusById(Long id, UpdateOrderStatusRequest request, String email);

    void deleteUserOrderById(Long id, String email);
}
