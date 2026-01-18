package com.intern.orderservice.service.broker;

import com.intern.orderservice.dto.payment.CreatePaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@NullMarked
public class OrderProducer {

    private final KafkaTemplate<String, CreatePaymentRequest> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, CreatePaymentRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCreatePayment(CreatePaymentRequest request) {
        kafkaTemplate.send("CREATE_ORDER", request.orderId().toString(), request);
        log.debug("Sent CREATE_ORDER event: {}", request);
    }
}
