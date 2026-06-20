package com.fabriciosanches.domain.port.input;

public record ProcessPaymentCallbackCommand(String paymentId, String callbackStatus) {
}
