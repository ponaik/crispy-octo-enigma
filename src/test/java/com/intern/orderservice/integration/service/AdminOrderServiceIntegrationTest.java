package com.intern.orderservice.integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.orderservice.dto.request.CreateOrderItemRequest;
import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.request.UpdateOrderStatusRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.dto.response.UserResponse;
import com.intern.orderservice.integration.CustomPostgreSQLContainer;
import com.intern.orderservice.model.Item;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.repository.ItemRepository;
import com.intern.orderservice.service.AdminOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
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
@AutoConfigureWireMock(port = 9099)
@ActiveProfiles("test")
@Tag("integration")
@Transactional
class AdminOrderServiceIntegrationTest extends CustomPostgreSQLContainer {

    @Autowired
    private AdminOrderService adminOrderService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse fakeUser;

    @BeforeEach
    void setup() throws Exception {
        fakeUser = new UserResponse(
                1L,
                "Alice",
                "Admin",
                LocalDate.of(1985, 5, 20),
                "alice.admin@example.com"
        );

        stubFor(get(urlEqualTo("/users/" + fakeUser.id()))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(fakeUser))
                        .withStatus(200)));
    }

    private Item createItem(String name, BigDecimal price) {
        Item item = new Item();
        item.setName(name);
        item.setPrice(price);
        return itemRepository.save(item);
    }

    @Test
    void testCreateOrder() {
        // Arrange: create item
        Item item = createItem("Server Rack", new BigDecimal("1500.00"));

        CreateOrderRequest request = new CreateOrderRequest(
                fakeUser.id(),
                List.of(new CreateOrderItemRequest(item.getId(), 1))
        );

        // Act
        OrderUserResponse response = adminOrderService.createOrder(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.user().id()).isEqualTo(fakeUser.id());
        assertThat(response.items()).hasSize(1);
    }

    @Test
    void testGetOrderById() {
        // Arrange: create item and order
        Item item = createItem("Router", new BigDecimal("200.00"));
        CreateOrderRequest request = new CreateOrderRequest(
                fakeUser.id(),
                List.of(new CreateOrderItemRequest(item.getId(), 2))
        );
        OrderUserResponse created = adminOrderService.createOrder(request);

        // Act
        Optional<OrderUserResponse> found = adminOrderService.getOrderById(created.id());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(created.id());
        assertThat(found.get().user().email()).isEqualTo(fakeUser.email());
    }

    @Test
    void testGetOrdersByIds() {
        // Arrange: create items and orders
        Item item1 = createItem("Switch", new BigDecimal("300.00"));
        Item item2 = createItem("Firewall", new BigDecimal("700.00"));

        OrderUserResponse order1 = adminOrderService.createOrder(
                new CreateOrderRequest(fakeUser.id(), List.of(new CreateOrderItemRequest(item1.getId(), 1)))
        );
        OrderUserResponse order2 = adminOrderService.createOrder(
                new CreateOrderRequest(fakeUser.id(), List.of(new CreateOrderItemRequest(item2.getId(), 1)))
        );

        // Act
        List<OrderUserResponse> foundOrders = adminOrderService.getOrdersByIds(List.of(order1.id(), order2.id()));

        // Assert
        assertThat(foundOrders).hasSize(2);
        assertThat(foundOrders.stream().map(OrderUserResponse::id))
                .containsExactlyInAnyOrder(order1.id(), order2.id());
    }

    @Test
    void testGetOrdersByStatuses() {
        // Arrange: create item and orders
        Item item1 = createItem("SSD", new BigDecimal("120.00"));
        Item item2 = createItem("HDD", new BigDecimal("80.00"));

        OrderUserResponse order1 = adminOrderService.createOrder(
                new CreateOrderRequest(fakeUser.id(), List.of(new CreateOrderItemRequest(item1.getId(), 1)))
        );
        OrderUserResponse order2 = adminOrderService.createOrder(
                new CreateOrderRequest(fakeUser.id(), List.of(new CreateOrderItemRequest(item2.getId(), 1)))
        );

        // Update one order’s status
        UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest(OrderStatus.CANCELLED);
        adminOrderService.updateOrderStatusById(order2.id(), updateRequest);

        // Act
        List<OrderUserResponse> cancelledOrders = adminOrderService.getOrdersByStatuses(List.of(OrderStatus.CANCELLED));
        List<OrderUserResponse> newOrders = adminOrderService.getOrdersByStatuses(List.of(OrderStatus.NEW));

        // Assert
        assertThat(cancelledOrders).hasSize(1);
        assertThat(cancelledOrders.get(0).status()).isEqualTo(OrderStatus.CANCELLED);

        assertThat(newOrders).hasSize(1);
        assertThat(newOrders.get(0).status()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    void testUpdateOrderStatusById() {
        // Arrange: create item and order
        Item item = createItem("UPS", new BigDecimal("400.00"));
        OrderUserResponse created = adminOrderService.createOrder(
                new CreateOrderRequest(fakeUser.id(), List.of(new CreateOrderItemRequest(item.getId(), 1)))
        );

        UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest(OrderStatus.SHIPPED);

        // Act
        OrderUserResponse updated = adminOrderService.updateOrderStatusById(created.id(), updateRequest);

        // Assert
        assertThat(updated.status()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void testDeleteOrderById() {
        // Arrange: create item and order
        Item item = createItem("Access Point", new BigDecimal("150.00"));
        OrderUserResponse created = adminOrderService.createOrder(
                new CreateOrderRequest(fakeUser.id(), List.of(new CreateOrderItemRequest(item.getId(), 1)))
        );

        // Act
        adminOrderService.deleteOrderById(created.id());

        // Assert
        Optional<OrderUserResponse> found = adminOrderService.getOrderById(created.id());
        assertThat(found).isEmpty();
    }
}
