package com.fabriciosanches.domain.port.input;

import java.time.Instant;

public record GetPaymentStatusResult(
        String paymentId,
        String orderId,
        String paymentMethod,
        String status,
        Instant createdAt
) {
}
