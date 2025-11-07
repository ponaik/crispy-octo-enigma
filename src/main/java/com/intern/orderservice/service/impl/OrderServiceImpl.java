package com.intern.orderservice.service.impl;

import com.intern.orderservice.dto.*;
import com.intern.orderservice.exception.ItemsNotFoundException;
import com.intern.orderservice.mapper.OrderMapper;
import com.intern.orderservice.model.Item;
import com.intern.orderservice.model.Order;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.repository.ItemRepository;
import com.intern.orderservice.repository.OrderRepository;
import com.intern.orderservice.service.OrderService;
import com.intern.orderservice.service.UserApiService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final UserApiService userApiService;


    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, ItemRepository itemRepository, OrderMapper orderMapper, UserApiService userApiService) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
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
        return orders.stream()
                .map(this::fetchUserThenMap)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderUserResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findAllByStatus(status);
        return orders.stream()
                .map(this::fetchUserThenMap)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderUserResponse> getOrdersByStatuses(Collection<OrderStatus> statuses) {
        List<Order> orders = orderRepository.findAllByStatusIn(statuses);
        return orders.stream()
                .map(this::fetchUserThenMap)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOrderById(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("Order with id " + id + " not found");
        }
        orderRepository.deleteById(id);
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
    public OrderUserResponse createOrder(CreateOrderRequest request) {
        log.debug("Creating order: {}", request);

        Set<Long> requestedItemIds = request.items().stream()
                .map(CreateOrderItemRequest::itemId)
                .collect(Collectors.toSet());

        Map<Long, Item> itemsById = itemRepository.findAllById(requestedItemIds).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        requestedItemIds.removeAll(itemsById.keySet());
        if (!requestedItemIds.isEmpty()) {
            throw new ItemsNotFoundException(requestedItemIds);
        }

        Order order = orderMapper.toOrder(request, itemsById);
        order.setCreationDate(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        order.getItems().forEach(item -> item.setOrder(order));

        Order saved = orderRepository.save(order);

        return fetchUserThenMap(saved);
    }

    private OrderUserResponse fetchUserThenMap(Order order) {
        UserResponse userById = userApiService.getUserById(order.getUserId());
        return orderMapper.toOrderUserResponse(order, userById);
    }

}
