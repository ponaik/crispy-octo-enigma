package com.intern.orderservice.service;

import com.intern.orderservice.dto.payment.CreatePaymentRequest;
import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.request.UpdateOrderStatusRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.model.enums.OrderStatus;
import com.intern.orderservice.service.broker.OrderProducer;
import com.intern.orderservice.service.helper.PaymentRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceFacade {

    private final OrderServiceAuthorizationDecorator delegate;
    private final OrderProducer orderProducer;
    private final PaymentRequestFactory paymentRequestFactory;

    @Autowired
    public OrderServiceFacade(OrderServiceAuthorizationDecorator delegate, OrderProducer orderProducer, PaymentRequestFactory paymentRequestFactory) {
        this.delegate = delegate;
        this.orderProducer = orderProducer;
        this.paymentRequestFactory = paymentRequestFactory;
    }

    public Optional<OrderUserResponse> getOrderById(Long id) {
        return delegate.getOrderById(id);
    }

    public List<OrderUserResponse> getOrdersByIds(Collection<Long> ids) {
        return delegate.getOrdersByIds(ids);
    }

    public List<OrderUserResponse> getOrdersByStatuses(Collection<OrderStatus> statuses) {
        return delegate.getOrdersByStatuses(statuses);
    }

    public OrderUserResponse createOrder(CreateOrderRequest request) {
        OrderUserResponse response = delegate.createOrder(request);
        CreatePaymentRequest createPaymentEvent = paymentRequestFactory.fromOrder(response);
        orderProducer.sendCreatePayment(createPaymentEvent);
        return response;
    }

    public OrderUserResponse updateOrderStatusById(Long id, UpdateOrderStatusRequest request) {
        return delegate.updateOrderStatusById(id, request);
    }

    public void deleteOrderById(Long id) {
        delegate.deleteOrderById(id);
    }
}
