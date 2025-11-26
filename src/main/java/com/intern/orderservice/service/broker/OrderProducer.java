package com.intern.orderservice.service.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.orderservice.dto.payment.CreatePaymentRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<@NonNull String, @NonNull String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderProducer(KafkaTemplate<@NonNull String, @NonNull String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    public void sendCreatePayment(CreatePaymentRequest request) {
        String payload = objectMapper.writeValueAsString(request);
        kafkaTemplate.send("CREATE_ORDER", request.orderId().toString(), payload);
        log.debug("Sent CREATE_ORDER event: {}", payload);
    }
}
