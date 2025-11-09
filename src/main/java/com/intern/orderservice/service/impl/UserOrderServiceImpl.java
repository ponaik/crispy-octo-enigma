package com.intern.orderservice.service.impl;

import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.request.CreateUserOrderRequest;
import com.intern.orderservice.dto.request.UpdateOrderStatusRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.dto.response.UserResponse;
import com.intern.orderservice.exception.StatusModificationIllegalAccessException;
import com.intern.orderservice.mapper.OrderMapper;
import com.intern.orderservice.model.Order;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.repository.OrderRepository;
import com.intern.orderservice.service.UserApiService;
import com.intern.orderservice.service.UserOrderService;
import com.intern.orderservice.service.helper.OrderCreationHelper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserOrderServiceImpl implements UserOrderService {

    private final UserApiService userApiService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderCreationHelper orderCreationHelper;

    @Autowired
    public UserOrderServiceImpl(UserApiService userApiService, OrderRepository orderRepository, OrderMapper orderMapper, OrderCreationHelper orderCreationHelper) {
        this.userApiService = userApiService;
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.orderCreationHelper = orderCreationHelper;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<OrderUserResponse> getUserOrderById(Long id, String email) {
        UserResponse userByEmail = userApiService.getUserByEmail(email);

        return orderRepository.findByIdAndUserId(id, userByEmail.id())
                .map(o -> orderMapper.toOrderUserResponse(o, userByEmail));
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderUserResponse> getUserOrdersByIds(Collection<Long> ids, String email) {
        UserResponse userByEmail = userApiService.getUserByEmail(email);

        return orderRepository.findAllByIdInAndUserId(ids, userByEmail.id()).stream()
                .map(o -> orderMapper.toOrderUserResponse(o, userByEmail))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderUserResponse> getUserOrdersByStatuses(Collection<OrderStatus> statuses, String email) {
        UserResponse userByEmail = userApiService.getUserByEmail(email);

        return orderRepository.findAllByStatusInAndUserId(statuses, userByEmail.id()).stream()
                .map(o -> orderMapper.toOrderUserResponse(o, userByEmail))
                .toList();
    }

    @Override
    public OrderUserResponse createUserOrder(CreateUserOrderRequest request, String email) {
        UserResponse userByEmail = userApiService.getUserByEmail(email);
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(userByEmail.id(), request.items());
        return orderCreationHelper.createOrderFromRequestAndUser(createOrderRequest, userByEmail);
    }

    @Override
    public OrderUserResponse updateUserOrderStatusById(Long id, UpdateOrderStatusRequest request, String email) {
        if (request.status() != OrderStatus.CANCELLED) {
            throw new StatusModificationIllegalAccessException(request.status());
        }

        UserResponse userByEmail = userApiService.getUserByEmail(email);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order with id " + id + " not found"));

        order.setStatus(request.status());
        Order updated = orderRepository.save(order);

        return orderMapper.toOrderUserResponse(updated, userByEmail);
    }

    @Override
    public void deleteUserOrderById(Long id, String email) {
        UserResponse userByEmail = userApiService.getUserByEmail(email);
        if (!orderRepository.existsByIdAndUserId(id, userByEmail.id())) {
            throw new EntityNotFoundException("Order with id " + id + " and userId " + userByEmail.id() + " not found");
        }
        orderRepository.deleteById(id);
    }

}
