package com.intern.orderservice.unit.service;

import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.request.UpdateOrderStatusRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.dto.response.UserResponse;
import com.intern.orderservice.exception.StatusModificationIllegalAccessException;
import com.intern.orderservice.mapper.OrderMapper;
import com.intern.orderservice.model.Order;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.repository.OrderRepository;
import com.intern.orderservice.service.UserApiService;
import com.intern.orderservice.service.helper.OrderCreationHelper;
import com.intern.orderservice.service.impl.UserOrderServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserOrderServiceImplTest {

    @Mock
    private UserApiService userApiService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderCreationHelper orderCreationHelper;

    @InjectMocks
    private UserOrderServiceImpl userOrderService;

    private final String email = "bob@example.com";
    private final Long userId = 42L;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse(userId, "Bob", "Builder", LocalDate.of(1990, 1, 1), email);
    }

    @Test
    void getUserOrderById_whenOrderExists_returnsMappedOrderUserResponse() {
        // given
        Long orderId = 100L;
        Order fakeOrder = org.mockito.Mockito.mock(Order.class);

        OrderUserResponse mapped = new OrderUserResponse(orderId, userResponse, OrderStatus.NEW, LocalDateTime.now(), Collections.emptyList());

        given(userApiService.getUserByEmail(email)).willReturn(userResponse);
        given(orderRepository.findByIdAndUserId(orderId, userId)).willReturn(Optional.of(fakeOrder));
        given(orderMapper.toOrderUserResponse(fakeOrder, userResponse)).willReturn(mapped);

        // when
        Optional<OrderUserResponse> result = userOrderService.getUserOrderById(orderId, email);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(mapped);
    }

    @Test
    void getUserOrderById_whenOrderNotFound_returnsEmptyOptional() {
        // given
        Long orderId = 101L;

        given(userApiService.getUserByEmail(email)).willReturn(userResponse);
        given(orderRepository.findByIdAndUserId(orderId, userId)).willReturn(Optional.empty());

        // when
        Optional<OrderUserResponse> result = userOrderService.getUserOrderById(orderId, email);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getUserOrdersByIds_returnsMappedList() {
        // given
        List<Long> ids = List.of(1L, 2L);
        Order o1 = org.mockito.Mockito.mock(Order.class);
        Order o2 = org.mockito.Mockito.mock(Order.class);

        OrderUserResponse r1 = new OrderUserResponse(1L, userResponse, OrderStatus.SHIPPED, LocalDateTime.now(), Collections.emptyList());
        OrderUserResponse r2 = new OrderUserResponse(2L, userResponse, OrderStatus.DELIVERED, LocalDateTime.now(), Collections.emptyList());

        given(userApiService.getUserByEmail(email)).willReturn(userResponse);
        given(orderRepository.findAllByIdInAndUserId(ids, userId)).willReturn(List.of(o1, o2));
        given(orderMapper.toOrderUserResponse(o1, userResponse)).willReturn(r1);
        given(orderMapper.toOrderUserResponse(o2, userResponse)).willReturn(r2);

        // when
        List<OrderUserResponse> result = userOrderService.getUserOrdersByIds(ids, email);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(r1, r2);
    }

    @Test
    void getUserOrdersByStatuses_returnsMappedList() {
        // given
        List<OrderStatus> statuses = List.of(OrderStatus.NEW, OrderStatus.PROCESSING);
        Order o1 = org.mockito.Mockito.mock(Order.class);

        OrderUserResponse r1 = new OrderUserResponse(11L, userResponse, OrderStatus.NEW, LocalDateTime.now(), Collections.emptyList());

        given(userApiService.getUserByEmail(email)).willReturn(userResponse);
        given(orderRepository.findAllByStatusInAndUserId(statuses, userId)).willReturn(List.of(o1));
        given(orderMapper.toOrderUserResponse(o1, userResponse)).willReturn(r1);

        // when
        List<OrderUserResponse> result = userOrderService.getUserOrdersByStatuses(statuses, email);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(r1);
    }

    @Test
    void createUserOrder_delegatesToOrderCreationHelper_andReturnsResponse() {
        // given
        CreateOrderRequest incoming = new CreateOrderRequest(null, Collections.emptyList());
        CreateOrderRequest expectedCreate = new CreateOrderRequest(userId, Collections.emptyList());
        OrderUserResponse createdResponse = new OrderUserResponse(999L, userResponse, OrderStatus.NEW, LocalDateTime.now(), Collections.emptyList());

        given(userApiService.getUserByEmail(email)).willReturn(userResponse);
        given(orderCreationHelper.createOrderFromRequestAndUser(expectedCreate, userResponse)).willReturn(createdResponse);

        // when
        OrderUserResponse result = userOrderService.createUserOrder(incoming, email);

        // then
        assertThat(result).isEqualTo(createdResponse);
    }

    @Test
    void updateUserOrderStatusById_whenCancelling_setsStatusAndReturnsMapped() {
        // given
        Long orderId = 55L;
        UpdateOrderStatusRequest cancelRequest = new UpdateOrderStatusRequest(OrderStatus.CANCELLED);
        Order persistent = org.mockito.Mockito.mock(Order.class);
        Order saved = org.mockito.Mockito.mock(Order.class);
        OrderUserResponse mapped = new OrderUserResponse(orderId, userResponse, OrderStatus.CANCELLED, LocalDateTime.now(), Collections.emptyList());

        given(userApiService.getUserByEmail(email)).willReturn(userResponse);
        given(orderRepository.findByIdAndUserId(orderId, userId)).willReturn(Optional.of(persistent));
        // simulate save returning updated order instance
        given(orderRepository.save(persistent)).willReturn(saved);
        given(orderMapper.toOrderUserResponse(saved, userResponse)).willReturn(mapped);

        // when
        OrderUserResponse result = userOrderService.updateUserOrderStatusById(orderId, cancelRequest, email);

        // then
        // verify status was set on the persistent entity
        verify(persistent).setStatus(OrderStatus.CANCELLED);
        assertThat(result).isEqualTo(mapped);
    }

    @Test
    void updateUserOrderStatusById_whenNonCancelled_throwsStatusModificationIllegalAccessException() {
        // given
        Long orderId = 66L;
        UpdateOrderStatusRequest attempt = new UpdateOrderStatusRequest(OrderStatus.SHIPPED);

        // when / then
        Assertions.assertThatThrownBy(() -> userOrderService.updateUserOrderStatusById(orderId, attempt, email))
                .isInstanceOf(StatusModificationIllegalAccessException.class)
                .hasMessageContaining("SHIPPED");
    }

    @Test
    void updateUserOrderStatusById_whenOrderNotFound_throwsEntityNotFoundException() {
        // given
        Long orderId = 77L;
        UpdateOrderStatusRequest cancelRequest = new UpdateOrderStatusRequest(OrderStatus.CANCELLED);

        given(userApiService.getUserByEmail(email)).willReturn(userResponse);
        given(orderRepository.findByIdAndUserId(orderId, userId)).willReturn(Optional.empty());

        // when / then
        Assertions.assertThatThrownBy(() -> userOrderService.updateUserOrderStatusById(orderId, cancelRequest, email))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order with id " + orderId);
    }

    @Test
    void deleteUserOrderById_whenExists_deletesOrder() {
        // given
        Long orderId = 120L;
        given(userApiService.getUserByEmail(email)).willReturn(userResponse);
        given(orderRepository.existsByIdAndUserId(orderId, userId)).willReturn(true);

        // when
        userOrderService.deleteUserOrderById(orderId, email);

        // then
        verify(orderRepository).deleteById(orderId);
    }

    @Test
    void deleteUserOrderById_whenNotExists_throwsEntityNotFoundException() {
        // given
        Long orderId = 121L;
        given(userApiService.getUserByEmail(email)).willReturn(userResponse);
        given(orderRepository.existsByIdAndUserId(orderId, userId)).willReturn(false);

        // when / then
        Assertions.assertThatThrownBy(() -> userOrderService.deleteUserOrderById(orderId, email))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order with id " + orderId);
    }
}
