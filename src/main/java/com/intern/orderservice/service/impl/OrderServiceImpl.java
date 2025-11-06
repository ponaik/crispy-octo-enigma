package com.intern.orderservice.service.impl;

import com.intern.orderservice.dto.CreateOrderItemRequest;
import com.intern.orderservice.dto.CreateOrderRequest;
import com.intern.orderservice.dto.OrderResponse;
import com.intern.orderservice.mapper.OrderMapper;
import com.intern.orderservice.model.Item;
import com.intern.orderservice.model.Order;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.repository.ItemRepository;
import com.intern.orderservice.repository.OrderRepository;
import com.intern.orderservice.service.OrderService;
import com.intern.orderservice.service.UserForwardingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;


    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, ItemRepository itemRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.orderMapper = orderMapper;
    }


    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toOrderResponse);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByIds(Collection<Long> ids) {
        List<Order> orders = orderRepository.findAllById(ids);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findAllByStatus(status);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatuses(Collection<OrderStatus> statuses) {
        List<Order> orders = orderRepository.findAllByStatusIn(statuses);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    public void deleteOrderById(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("Order with id " + id + " not found");
        }
        orderRepository.deleteById(id);
    }

//    /**
//     * Update an existing order by id using UpdateOrderRequest. Returns updated DTO wrapped in Optional.
//     * If order not found, or referenced items are missing, returns Optional.empty().
//     *
//     * This method replaces order items with those supplied by request.
//     */
//    public Optional<OrderResponse> updateOrderById(Long id, UpdateOrderRequest request) {
//        Optional<Order> maybeOrder = orderRepository.findById(id);
//        if (maybeOrder.isEmpty()) {
//            return Optional.empty();
//        }
//
//        // collect item ids referenced by request
//        Set<Long> itemIds = request.getItems().stream()
//                .map(ci -> ci.getItemId())
//                .collect(Collectors.toSet());
//
//        List<Item> items = itemRepository.findAllById(itemIds);
//        if (items.size() != itemIds.size()) {
//            // some items referenced do not exist
//            return Optional.empty();
//        }
//        Map<Long, Item> itemsById = items.stream()
//                .collect(Collectors.toMap(Item::getId, i -> i));
//
//        Order existing = maybeOrder.get();
//
//        // Use mapper to create an Order entity based on Update request.
//        // Since mapper toOrder ignores id/creationDate/status, we either
//        // create a new Order via mapper and then copy necessary fields,
//        // or update in-place. Here we'll create via mapper then copy
//        // mutable fields (order items) to existing entity.
//        Order fromRequest = orderMapper.toOrder(request.toCreateRequest(), itemsById);
//
//        // Update mutable fields (assumes Order has setOrderItems() and setStatus if provided)
//        existing.setOrderItems(fromRequest.getOrderItems());
//        // Only update status if request provides it (example)
//        if (request.getStatus() != null) {
//            existing.setStatus(request.getStatus());
//        }
//
//        Order saved = orderRepository.save(existing);
//        return Optional.of(orderMapper.toOrderResponse(saved));

//    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.debug("Creating order: {}", request);

        if (request.items().isEmpty()) {
            throw new RuntimeException("Order Items are empty");
        }

        List<Long> itemIds = request.items().stream().map(CreateOrderItemRequest::itemId).toList();
        if (itemIds.isEmpty()) {
            throw new RuntimeException("Order Items are empty");
        }

        Map<Long, Item> itemsById = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        // itemsById exception when smaller than itemIds
//        if (itemIds.isEmpty()) {
//            throw new RuntimeException("Order Items are empty");
//        }

        Order order = orderMapper.toOrder(request, itemsById);
        order.setCreationDate(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        order.getItems().forEach(item -> item.setOrder(order));

        Order saved = orderRepository.save(order);

        return orderMapper.toOrderResponse(saved);
    }

}
