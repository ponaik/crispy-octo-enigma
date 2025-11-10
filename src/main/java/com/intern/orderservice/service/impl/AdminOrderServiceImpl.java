package com.intern.orderservice.service.impl;

import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.request.UpdateOrderStatusRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.dto.response.UserResponse;
import com.intern.orderservice.mapper.OrderMapper;
import com.intern.orderservice.model.Order;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.repository.OrderRepository;
import com.intern.orderservice.service.AdminOrderService;
import com.intern.orderservice.service.UserApiService;
import com.intern.orderservice.service.helper.OrderCreationHelper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderCreationHelper orderCreationHelper;
    private final OrderMapper orderMapper;
    private final UserApiService userApiService;


    @Autowired
    public AdminOrderServiceImpl(OrderRepository orderRepository, OrderCreationHelper orderCreationHelper, OrderMapper orderMapper, UserApiService userApiService) {
        this.orderRepository = orderRepository;
        this.orderCreationHelper = orderCreationHelper;
        this.orderMapper = orderMapper;
        this.userApiService = userApiService;
    }


    @Transactional(readOnly = true)
    @Override
    public Optional<OrderUserResponse> getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(this::fetchUserThenMap);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderUserResponse> getOrdersByIds(Collection<Long> ids) {
        List<Order> orders = orderRepository.findAllById(ids);
        Map<Long, UserResponse> usersById = fetchUsersMapFromOrders(orders);
        return mapValidOrderUserResponses(orders, usersById);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderUserResponse> getOrdersByStatuses(Collection<OrderStatus> statuses) {
        List<Order> orders = orderRepository.findAllByStatusIn(statuses);
        Map<Long, UserResponse> usersById = fetchUsersMapFromOrders(orders);
        return mapValidOrderUserResponses(orders, usersById);
    }

    @Override
    public OrderUserResponse createOrder(CreateOrderRequest request) {
        UserResponse userById = userApiService.getUserById(request.userId());
        if (userById == null || userById.id() == null) {
            throw new EntityNotFoundException("User with id: " + request.userId() + " not found");
        }
        return orderCreationHelper.createOrderFromRequestAndUser(request, userById);
    }

    @Override
    public OrderUserResponse updateOrderStatusById(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order with id " + id + " not found"));

        order.setStatus(request.status());
        Order updated = orderRepository.save(order);

        return fetchUserThenMap(updated);
    }

    @Override
    public void deleteOrderById(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("Order with id " + id + " not found");
        }
        orderRepository.deleteById(id);
    }

    private List<OrderUserResponse> mapValidOrderUserResponses(List<Order> orders, Map<Long, UserResponse> usersById) {
        return orders.stream()
                .map(o -> orderMapper.toOrderUserResponseList(o, usersById))
                .filter(o -> o.user() != null)
                .toList();
    }

    private Map<Long, UserResponse> fetchUsersMapFromOrders(List<Order> orders) {
        ConcurrentHashMap<Long, UserResponse> usersById = new ConcurrentHashMap<>();
        orders.stream()
                .map(Order::getUserId)
                .forEach(userId -> usersById.computeIfAbsent(userId, userApiService::getUserById));
        return usersById;
    }

    private OrderUserResponse fetchUserThenMap(Order order) {
        UserResponse userById = userApiService.getUserById(order.getUserId());
        return orderMapper.toOrderUserResponse(order, userById);
    }
}
