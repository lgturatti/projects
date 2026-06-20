package com.fabriciosanches.domain.port.input;

public record ProcessPaymentCommand(String orderId, String paymentMethod) {
}
