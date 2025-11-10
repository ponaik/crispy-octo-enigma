package com.intern.orderservice.service.helper;

import com.intern.orderservice.dto.request.CreateOrderItemRequest;
import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.dto.response.UserResponse;
import com.intern.orderservice.exception.ItemsNotFoundException;
import com.intern.orderservice.mapper.OrderMapper;
import com.intern.orderservice.model.Item;
import com.intern.orderservice.model.Order;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.repository.ItemRepository;
import com.intern.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrderCreationHelper {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderCreationHelper(ItemRepository itemRepository,
                               OrderRepository orderRepository,
                               OrderMapper orderMapper) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    public OrderUserResponse createOrderFromRequestAndUser(CreateOrderRequest request, UserResponse user) {
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
        order.setUserId(user.id());
        order.setCreationDate(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        order.getItems().forEach(item -> item.setOrder(order));

        Order saved = orderRepository.save(order);

        return orderMapper.toOrderUserResponse(saved, user);
    }
}
