package com.intern.orderservice.unit.service;

import com.intern.orderservice.dto.request.CreateOrderItemRequest;
import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.request.UpdateOrderStatusRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.dto.response.UserResponse;
import com.intern.orderservice.mapper.OrderMapper;
import com.intern.orderservice.model.Order;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.repository.OrderRepository;
import com.intern.orderservice.service.UserApiService;
import com.intern.orderservice.service.helper.OrderCreationHelper;
import com.intern.orderservice.service.impl.AdminOrderServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminOrderServiceImplTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderCreationHelper orderCreationHelper;
    @Mock OrderMapper orderMapper;
    @Mock UserApiService userApiService;

    @InjectMocks AdminOrderServiceImpl service;

    private static final Long USER_ID = 10L;
    private static final Long ORDER_ID = 100L;

    // Helpers to build sample objects
    private Order sampleOrder(Long id, Long userId, OrderStatus status) {
        Order o = new Order();
        o.setId(id);
        o.setUserId(userId);
        o.setStatus(status);
        o.setCreationDate(LocalDateTime.now());
        o.setItems(Collections.emptyList());
        return o;
    }

    private UserResponse sampleUser(Long id) {
        return new UserResponse(id, "John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com");
    }

    private OrderUserResponse sampleOrderUserResponse(Long id, UserResponse user, OrderStatus status) {
        return new OrderUserResponse(id, user, status, LocalDateTime.now(), Collections.emptyList());
    }

    // getOrderById - found
    @Test
    void givenOrderExists_whenGetOrderById_thenReturnsMappedResponse() {
        Order order = sampleOrder(ORDER_ID, USER_ID, OrderStatus.NEW);
        UserResponse user = sampleUser(USER_ID);
        OrderUserResponse mapped = sampleOrderUserResponse(ORDER_ID, user, order.getStatus());

        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
        given(userApiService.getUserById(USER_ID)).willReturn(user);
        given(orderMapper.toOrderUserResponse(order, user)).willReturn(mapped);

        Optional<OrderUserResponse> result = service.getOrderById(ORDER_ID);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(mapped);
    }

    // getOrderById - not found
    @Test
    void givenOrderMissing_whenGetOrderById_thenReturnsEmpty() {
        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.empty());

        Optional<OrderUserResponse> result = service.getOrderById(ORDER_ID);

        assertThat(result).isEmpty();
        verifyNoMoreInteractions(userApiService, orderMapper);
    }

    // getOrdersByIds - users missing filtered out
    @Test
    void givenOrders_whenGetOrdersByIds_thenOnlyReturnResponsesWithUsers() {
        Order o1 = sampleOrder(1L, 1L, OrderStatus.PROCESSING);
        Order o2 = sampleOrder(2L, 2L, OrderStatus.PROCESSING);
        List<Order> orders = List.of(o1, o2);

        UserResponse user1 = sampleUser(1L);

        given(orderRepository.findAllById(List.of(1L, 2L))).willReturn(orders);
        given(userApiService.getUserById(1L)).willReturn(user1);
        given(userApiService.getUserById(2L)).willReturn(null);

        OrderUserResponse resp1 = sampleOrderUserResponse(1L, user1, o1.getStatus());
        OrderUserResponse resp2 = sampleOrderUserResponse(2L, null, o2.getStatus());

        Map<Long, UserResponse> usersMap = Map.of(1L, user1);
        given(orderMapper.toOrderUserResponseList(o1, usersMap)).willReturn(resp1);
        given(orderMapper.toOrderUserResponseList(o2, usersMap)).willReturn(resp2);

        List<OrderUserResponse> results = service.getOrdersByIds(List.of(1L, 2L));

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(resp1);
    }

    // getOrdersByStatuses - similar behavior
    @Test
    void givenOrders_whenGetOrdersByStatuses_thenOnlyReturnResponsesWithUsers() {
        Order o1 = sampleOrder(3L, 3L, OrderStatus.SHIPPED);
        Order o2 = sampleOrder(4L, 4L, OrderStatus.SHIPPED);
        List<Order> orders = List.of(o1, o2);

        UserResponse u3 = sampleUser(3L);
        given(orderRepository.findAllByStatusIn(Set.of(OrderStatus.SHIPPED))).willReturn(orders);
        given(userApiService.getUserById(3L)).willReturn(u3);
        given(userApiService.getUserById(4L)).willReturn(null);

        OrderUserResponse r3 = sampleOrderUserResponse(3L, u3, o1.getStatus());
        OrderUserResponse r4 = sampleOrderUserResponse(4L, null, o2.getStatus());

        given(orderMapper.toOrderUserResponseList(eq(o1), anyMap())).willReturn(r3);
        given(orderMapper.toOrderUserResponseList(eq(o2), anyMap())).willReturn(r4);




        List<OrderUserResponse> results = service.getOrdersByStatuses(Set.of(OrderStatus.SHIPPED));

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(r3);
    }

    // createOrder - success
    @Test
    void givenValidCreateRequestAndUserExists_whenCreateOrder_thenDelegatesToHelperAndReturns() {
        CreateOrderItemRequest itemReq = new CreateOrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest(USER_ID, List.of(itemReq));
        UserResponse user = sampleUser(USER_ID);
        OrderUserResponse created = sampleOrderUserResponse(200L, user, OrderStatus.NEW);

        given(userApiService.getUserById(USER_ID)).willReturn(user);
        given(orderCreationHelper.createOrderFromRequestAndUser(request, user)).willReturn(created);

        OrderUserResponse result = service.createOrder(request);

        assertThat(result).isEqualTo(created);
    }

    // createOrder - user not found -> exception
    @Test
    void givenCreateRequestAndUserMissing_whenCreateOrder_thenThrowsEntityNotFoundException() {
        CreateOrderItemRequest itemReq = new CreateOrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest(USER_ID, List.of(itemReq));

        given(userApiService.getUserById(USER_ID)).willReturn(null);

        Throwable thrown = catchThrowable(() -> service.createOrder(request));

        assertThat(thrown).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with id: " + USER_ID + " not found");
        verifyNoMoreInteractions(orderCreationHelper);
    }

    // updateOrderStatusById - success
    @Test
    void givenExistingOrder_whenUpdateOrderStatusById_thenSavesAndReturnsMapped() {
        Order existing = sampleOrder(ORDER_ID, USER_ID, OrderStatus.NEW);
        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(OrderStatus.DELIVERED);
        Order saved = sampleOrder(ORDER_ID, USER_ID, OrderStatus.DELIVERED);

        UserResponse user = sampleUser(USER_ID);
        OrderUserResponse mapped = sampleOrderUserResponse(ORDER_ID, user, OrderStatus.DELIVERED);

        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(existing));
        given(orderRepository.save(existing)).willReturn(saved);
        given(userApiService.getUserById(USER_ID)).willReturn(user);
        given(orderMapper.toOrderUserResponse(saved, user)).willReturn(mapped);

        OrderUserResponse result = service.updateOrderStatusById(ORDER_ID, req);

        assertThat(result).isEqualTo(mapped);
    }

    // updateOrderStatusById - missing -> exception
    @Test
    void givenMissingOrder_whenUpdateOrderStatusById_thenThrowsEntityNotFoundException() {
        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.empty());
        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(OrderStatus.DELIVERED);

        Throwable thrown = catchThrowable(() -> service.updateOrderStatusById(ORDER_ID, req));

        assertThat(thrown).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order with id " + ORDER_ID + " not found");
        verifyNoMoreInteractions(orderRepository, userApiService, orderMapper);
    }

    // deleteOrderById - success
    @Test
    void givenExistingOrder_whenDeleteOrderById_thenDeletes() {
        given(orderRepository.existsById(ORDER_ID)).willReturn(true);

        service.deleteOrderById(ORDER_ID);

        verify(orderRepository, times(1)).deleteById(ORDER_ID);
    }

    // deleteOrderById - missing -> exception
    @Test
    void givenMissingOrder_whenDeleteOrderById_thenThrowsEntityNotFoundException() {
        given(orderRepository.existsById(ORDER_ID)).willReturn(false);

        Throwable thrown = catchThrowable(() -> service.deleteOrderById(ORDER_ID));

        assertThat(thrown).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order with id " + ORDER_ID + " not found");
        verifyNoMoreInteractions(orderRepository);
    }
}
