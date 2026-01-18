package com.intern.orderservice.service.helper;


import com.intern.orderservice.dto.payment.CreatePaymentRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@NoArgsConstructor
public class PaymentRequestFactory {

    public CreatePaymentRequest fromOrder(OrderUserResponse order) {
        BigDecimal totalAmount = order.items().stream()
                .map(item -> item.item().price()
                        .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CreatePaymentRequest(
                order.id(),
                order.user().id(),
                totalAmount
        );
    }
}
