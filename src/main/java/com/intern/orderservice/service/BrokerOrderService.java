package com.intern.orderservice.service;

import com.intern.orderservice.dto.payment.PaymentResponse;

public interface BrokerOrderService {

    void updateOrderStatusFromPayment(PaymentResponse response);
}
