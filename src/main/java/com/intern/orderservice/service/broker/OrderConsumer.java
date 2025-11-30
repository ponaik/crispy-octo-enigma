package com.intern.orderservice.service.broker;

import com.intern.orderservice.dto.payment.PaymentResponse;
import com.intern.orderservice.service.BrokerOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderConsumer {

    private final BrokerOrderService brokerOrderService;

    @Autowired
    public OrderConsumer(BrokerOrderService brokerOrderService) {
        this.brokerOrderService = brokerOrderService;
    }

    @KafkaListener(topics = "UPDATE_PAYMENT", groupId = "order-service")
    public void consumePaymentResponse(PaymentResponse event) {
        log.debug("Order Service received UPDATE_PAYMENT event: {}", event);
        brokerOrderService.updateOrderStatusFromPayment(event);
    }
}
