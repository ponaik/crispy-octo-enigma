package com.intern.orderservice.unit.helper;

import com.intern.orderservice.dto.request.CreateOrderItemRequest;
import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.dto.response.UserResponse;
import com.intern.orderservice.exception.ItemsNotFoundException;
import com.intern.orderservice.mapper.OrderMapper;
import com.intern.orderservice.model.Item;
import com.intern.orderservice.model.Order;
import com.intern.orderservice.model.OrderItem;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.repository.ItemRepository;
import com.intern.orderservice.repository.OrderRepository;
import com.intern.orderservice.service.helper.OrderCreationHelper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class OrderCreationHelperTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @InjectMocks
    private OrderCreationHelper helper;

    @Test
    void testCreateOrderFromRequestAndUserSuccess() {
        // Arrange
        Item item = new Item();
        item.setId(1L);
        item.setName("Laptop");
        item.setPrice(new BigDecimal("1200.00"));


        CreateOrderRequest request = new CreateOrderRequest(
                null,
                List.of(new CreateOrderItemRequest(1L, 2))
        );
        UserResponse user = new UserResponse(10L, "John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com");

        Order order = new Order();
        order.setId(100L);
        OrderItem orderItem = new OrderItem(1L, order, item, 2);
        order.setItems(List.of(orderItem));

        Order savedOrder = new Order();
        savedOrder.setId(100L);
        savedOrder.setItems(List.of(orderItem));
        savedOrder.setUserId(user.id());
        savedOrder.setStatus(OrderStatus.NEW);

        OrderUserResponse response = new OrderUserResponse(100L, user, OrderStatus.NEW, null, Collections.emptyList());

        given(itemRepository.findAllById(anySet())).willReturn(List.of(item));
        given(orderMapper.toOrder(eq(request), anyMap())).willReturn(order);
        given(orderRepository.save(order)).willReturn(savedOrder);
        given(orderMapper.toOrderUserResponse(savedOrder, user)).willReturn(response);

        // Act
        OrderUserResponse result = helper.createOrderFromRequestAndUser(request, user);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.user().id()).isEqualTo(10L);
        assertThat(result.status()).isEqualTo(OrderStatus.NEW);

        then(orderRepository).should().save(order);
        then(orderMapper).should().toOrderUserResponse(savedOrder, user);
    }

    @Test
    void testCreateOrderThrowsItemsNotFoundException() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
                null,
                List.of(new CreateOrderItemRequest(99L, 1))
        );
        UserResponse user = new UserResponse(10L, "John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com");

        given(itemRepository.findAllById(anySet())).willReturn(List.of()); // no items found

        // Act + Assert
        assertThatThrownBy(() -> helper.createOrderFromRequestAndUser(request, user))
                .isInstanceOf(ItemsNotFoundException.class);
    }
}
