package com.fabriciosanches.domain.port.output;

import java.math.BigDecimal;

public interface PaymentGatewayClientPort {

    PaymentGatewayResult charge(String orderId, BigDecimal amount, String paymentMethod);

    record PaymentGatewayResult(String transactionId, String status) {
    }
}
