package com.intern.orderservice.service.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.orderservice.dto.payment.PaymentResponse;
import com.intern.orderservice.dto.request.UpdateOrderStatusRequest;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.service.AdminOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderConsumer {

    private final ObjectMapper objectMapper;
    private final AdminOrderService adminOrderService;

    public OrderConsumer(ObjectMapper objectMapper, AdminOrderService adminOrderService) {
        this.objectMapper = objectMapper;
        this.adminOrderService = adminOrderService;
    }

    @KafkaListener(topics = "CREATE_PAYMENT", groupId = "order-service")
    public void consumePaymentResponse(String message) throws JsonProcessingException {
        PaymentResponse response = objectMapper.readValue(message, PaymentResponse.class);
        log.debug("Order Service received PAYMENT_STATUS event: {}", response);

        UpdateOrderStatusRequest updateStatusRequest = new UpdateOrderStatusRequest(OrderStatus.PROCESSING);
        adminOrderService.updateOrderStatusById(response.orderId(), updateStatusRequest);
    }
}
