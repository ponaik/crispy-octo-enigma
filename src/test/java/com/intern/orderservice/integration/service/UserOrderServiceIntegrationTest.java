package com.intern.orderservice.integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.orderservice.dto.request.CreateOrderItemRequest;
import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.request.UpdateOrderStatusRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.dto.response.UserResponse;
import com.intern.orderservice.integration.CustomPostgreSQLContainer;
import com.intern.orderservice.integration.NoSecurityConfig;
import com.intern.orderservice.model.Item;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.repository.ItemRepository;
import com.intern.orderservice.service.UserOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({NoSecurityConfig.class})
@AutoConfigureWireMock(port = 9099)
@ActiveProfiles("test")
@Tag("integration")
@Transactional
class UserOrderServiceIntegrationTest extends CustomPostgreSQLContainer {

    @Autowired
    private UserOrderService userOrderService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse fakeUser;

    @BeforeEach
    void setup() throws Exception {
        fakeUser = new UserResponse(
                1L,
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "john.doe@example.com"
        );

        stubFor(get(urlEqualTo("/users/search?email=" + fakeUser.email()))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(fakeUser))
                        .withStatus(200)));

        stubFor(get(urlEqualTo("/users/" + fakeUser.id()))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(fakeUser))
                        .withStatus(200)));
    }

    @Test
    void testCreateUserOrder() {
        // Arrange
        Item item = new Item();
        item.setName("Laptop");
        item.setPrice(new BigDecimal("1200.00"));
        Item savedItem = itemRepository.save(item);

        CreateOrderRequest request = new CreateOrderRequest(
                null, // ignored for non-admin
                List.of(new CreateOrderItemRequest(savedItem.getId(), 2))
        );

        // Act
        OrderUserResponse response = userOrderService.createUserOrder(request, fakeUser.email());

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.user().email()).isEqualTo(fakeUser.email());
        assertThat(response.items()).hasSize(1);
    }

    @Test
    void testGetUserOrderById() {
        // Arrange: create order first
        Item item = new Item();
        item.setName("Laptop");
        item.setPrice(new BigDecimal("1200.00"));
        Item savedItem = itemRepository.save(item);

        CreateOrderRequest request = new CreateOrderRequest(
                null,
                List.of(new CreateOrderItemRequest(savedItem.getId(), 1))
        );
        OrderUserResponse created = userOrderService.createUserOrder(request, fakeUser.email());

        // Act
        Optional<OrderUserResponse> found = userOrderService.getUserOrderById(created.id(), fakeUser.email());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(created.id());
        assertThat(found.get().user().id()).isEqualTo(fakeUser.id());
    }

    @Test
    void testUpdateUserOrderStatus() {
        // Arrange: create order first
        Item item = new Item();
        item.setName("Laptop");
        item.setPrice(new BigDecimal("1200.00"));
        Item savedItem = itemRepository.save(item);

        CreateOrderRequest request = new CreateOrderRequest(
                null,
                List.of(new CreateOrderItemRequest(savedItem.getId(), 5))
        );
        OrderUserResponse created = userOrderService.createUserOrder(request, fakeUser.email());

        UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest(OrderStatus.CANCELLED);

        // Act
        OrderUserResponse updated = userOrderService.updateUserOrderStatusById(created.id(), updateRequest, fakeUser.email());

        // Assert
        assertThat(updated.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void testDeleteUserOrder() {
        // Arrange: create order first
        Item item = new Item();
        item.setName("Laptop");
        item.setPrice(new BigDecimal("1200.00"));
        Item savedItem = itemRepository.save(item);

        CreateOrderRequest request = new CreateOrderRequest(
                null,
                List.of(new CreateOrderItemRequest(savedItem.getId(), 3))
        );
        OrderUserResponse created = userOrderService.createUserOrder(request, fakeUser.email());

        // Act
        userOrderService.deleteUserOrderById(created.id(), fakeUser.email());

        // Assert: order should not be found anymore
        Optional<OrderUserResponse> found = userOrderService.getUserOrderById(created.id(), fakeUser.email());
        assertThat(found).isEmpty();
    }

    @Test
    void testGetUserOrdersByIds() {
        // Arrange: create items and orders
        Item item1 = createItem("Tablet", new BigDecimal("500.00"));
        Item item2 = createItem("Monitor", new BigDecimal("300.00"));

        OrderUserResponse order1 = userOrderService.createUserOrder(
                new CreateOrderRequest(null, List.of(new CreateOrderItemRequest(item1.getId(), 1))),
                fakeUser.email()
        );
        OrderUserResponse order2 = userOrderService.createUserOrder(
                new CreateOrderRequest(null, List.of(new CreateOrderItemRequest(item2.getId(), 2))),
                fakeUser.email()
        );

        // Act
        List<OrderUserResponse> foundOrders = userOrderService.getUserOrdersByIds(
                List.of(order1.id(), order2.id()), fakeUser.email()
        );

        // Assert
        assertThat(foundOrders).hasSize(2);
        assertThat(foundOrders.stream().map(OrderUserResponse::id))
                .containsExactlyInAnyOrder(order1.id(), order2.id());
    }

    @Test
    void testGetUserOrdersByStatuses() {
        // Arrange: create items and orders
        Item item1 = createItem("Keyboard", new BigDecimal("100.00"));
        Item item2 = createItem("Mouse", new BigDecimal("50.00"));

        OrderUserResponse order1 = userOrderService.createUserOrder(
                new CreateOrderRequest(null, List.of(new CreateOrderItemRequest(item1.getId(), 1))),
                fakeUser.email()
        );
        OrderUserResponse order2 = userOrderService.createUserOrder(
                new CreateOrderRequest(null, List.of(new CreateOrderItemRequest(item2.getId(), 1))),
                fakeUser.email()
        );

        // Cancel one order to change its status
        UpdateOrderStatusRequest cancelRequest = new UpdateOrderStatusRequest(OrderStatus.CANCELLED);
        userOrderService.updateUserOrderStatusById(order2.id(), cancelRequest, fakeUser.email());

        // Act: query by statuses
        List<OrderUserResponse> cancelledOrders = userOrderService.getUserOrdersByStatuses(
                List.of(OrderStatus.CANCELLED), fakeUser.email()
        );

        List<OrderUserResponse> newOrders = userOrderService.getUserOrdersByStatuses(
                List.of(OrderStatus.NEW), fakeUser.email()
        );

        // Assert
        assertThat(cancelledOrders).hasSize(1);
        assertThat(cancelledOrders.get(0).status()).isEqualTo(OrderStatus.CANCELLED);

        assertThat(newOrders).hasSize(1);
        assertThat(newOrders.get(0).status()).isEqualTo(OrderStatus.NEW);
    }

    private Item createItem(String name, BigDecimal price) {
        Item item = new Item();
        item.setName(name);
        item.setPrice(price);
        return itemRepository.save(item);
    }
}
