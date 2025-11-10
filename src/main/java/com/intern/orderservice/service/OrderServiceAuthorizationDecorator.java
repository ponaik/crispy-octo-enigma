package com.intern.orderservice.service;

import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.request.UpdateOrderStatusRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.model.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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
        } else if (authorizationService.isUser()) {
            return userOrderService.getUserOrderById(id, authorizationService.getEmail());
        } else {
            throw new AccessDeniedException("Access denied: unauthorized role");
        }
    }

    public List<OrderUserResponse> getOrdersByIds(Collection<Long> ids) {
        if (authorizationService.isAdmin()) {
            return adminOrderService.getOrdersByIds(ids);
        } else if (authorizationService.isUser()) {
            return userOrderService.getUserOrdersByIds(ids, authorizationService.getEmail());
        } else {
            throw new AccessDeniedException("Access denied: unauthorized role");
        }
    }

    public List<OrderUserResponse> getOrdersByStatuses(Collection<OrderStatus> statuses) {
        if (authorizationService.isAdmin()) {
            return adminOrderService.getOrdersByStatuses(statuses);
        } else if (authorizationService.isUser()) {
            return userOrderService.getUserOrdersByStatuses(statuses, authorizationService.getEmail());
        } else {
            throw new AccessDeniedException("Access denied: unauthorized role");
        }
    }

    public OrderUserResponse createOrder(CreateOrderRequest request) {
        if (authorizationService.isAdmin()) {
            return adminOrderService.createOrder(request);
        } else if (authorizationService.isUser()) {
            return userOrderService.createUserOrder(request, authorizationService.getEmail());
        } else {
            throw new AccessDeniedException("Access denied: unauthorized role");
        }
    }

    public OrderUserResponse updateOrderStatusById(Long id, UpdateOrderStatusRequest request) {
        if (authorizationService.isAdmin()) {
            return adminOrderService.updateOrderStatusById(id, request);
        } else if (authorizationService.isUser()) {
            return userOrderService.updateUserOrderStatusById(id, request, authorizationService.getEmail());
        } else {
            throw new AccessDeniedException("Access denied: unauthorized role");
        }
    }

    public void deleteOrderById(Long id) {
        if (authorizationService.isAdmin()) {
            adminOrderService.deleteOrderById(id);
        } else if (authorizationService.isUser()) {
            userOrderService.deleteUserOrderById(id, authorizationService.getEmail());
        } else {
            throw new AccessDeniedException("Access denied: unauthorized role");
        }
    }
}
