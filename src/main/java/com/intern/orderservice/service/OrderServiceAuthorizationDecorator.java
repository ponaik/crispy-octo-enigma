package com.intern.orderservice.service;

import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.request.CreateUserOrderRequest;
import com.intern.orderservice.dto.request.UpdateOrderStatusRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.exception.CreateOrderIllegalAccessException;
import com.intern.orderservice.model.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceAuthorizationDecorator {

    private final AdminOrderService adminOrderService;
    private final UserOrderService userOrderService;
    private final AuthorizationService authorizationService;

    @Autowired
    public OrderServiceAuthorizationDecorator(AdminOrderService adminOrderService, UserOrderService userOrderService, AuthorizationService authorizationService) {
        this.adminOrderService = adminOrderService;
        this.userOrderService = userOrderService;
        this.authorizationService = authorizationService;
    }

    public Optional<OrderUserResponse> getOrderById(Long id) {
        if (authorizationService.isAdmin()) {
            return adminOrderService.getOrderById(id);
        }
        return userOrderService.getUserOrderById(id, authorizationService.getEmail());
    }

    public List<OrderUserResponse> getOrdersByIds(Collection<Long> ids) {
        if (authorizationService.isAdmin()) {
            return adminOrderService.getOrdersByIds(ids);
        }
        return userOrderService.getUserOrdersByIds(ids, authorizationService.getEmail());
    }

    public List<OrderUserResponse> getOrdersByStatuses(Collection<OrderStatus> statuses) {
        if (authorizationService.isAdmin()) {
            return adminOrderService.getOrdersByStatuses(statuses);
        }
        return userOrderService.getUserOrdersByStatuses(statuses, authorizationService.getEmail());
    }

    public OrderUserResponse createOrder(CreateOrderRequest request) {
        if (!authorizationService.isAdmin()) {
            throw new CreateOrderIllegalAccessException(request);
        }
        return adminOrderService.createOrder(request);
    }

    public OrderUserResponse createOrder(CreateUserOrderRequest request) {
        return userOrderService.createUserOrder(request, authorizationService.getEmail());
    }

    public OrderUserResponse updateOrderStatusById(Long id, UpdateOrderStatusRequest request) {
        if (authorizationService.isAdmin()) {
            return adminOrderService.updateOrderStatusById(id, request);
        }
        return userOrderService.updateUserOrderStatusById(id, request, authorizationService.getEmail());
    }

    public void deleteOrderById(Long id) {
        if (authorizationService.isAdmin()) {
            adminOrderService.deleteOrderById(id);
            return;
        }
        userOrderService.deleteUserOrderById(id, authorizationService.getEmail());
    }
}
