package com.intern.orderservice.service.impl;

import com.intern.orderservice.dto.payment.PaymentResponse;
import com.intern.orderservice.exception.UnknownPaymentStatusException;
import com.intern.orderservice.model.Order;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.repository.OrderRepository;
import com.intern.orderservice.service.BrokerOrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class KafkaOrderServiceImpl implements BrokerOrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public KafkaOrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    @Override
    public void updateOrderStatusFromPayment(PaymentResponse response) {
        if (response.orderId() == null) {
            log.error("Received PaymentResponse with null orderId: {}", response);
            throw new IllegalArgumentException("PaymentResponse must contain a non-null orderId.");
        }
        if (response.status() == null) {
            log.error("Received PaymentResponse with null status: {}", response);
            throw new IllegalArgumentException("PaymentResponse must contain a non-null status.");
        }

        Long id = response.orderId();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order with id " + id + " not found"));

        switch (response.status()) {
            case PENDING -> order.setStatus(OrderStatus.PROCESSING);
            case SUCCESS -> order.setStatus(OrderStatus.PAID);
            case FAILED, REFUNDED -> order.setStatus(OrderStatus.CANCELLED);
            default -> {
                log.error("Unknown payment status received: {}", response.status());
                throw new UnknownPaymentStatusException(response.status());
            }
        }

        orderRepository.save(order);
        log.debug("Order with id {} status updated by Kafka: {}", order.getId(), order.getStatus());
    }
}
