package com.fabriciosanches.domain.port.input;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record GetOrderByIdResult(
        String orderId,
        String customerId,
        String status,
        BigDecimal totalAmount,
        int paymentFailureCount,
        Instant createdAt,
        List<OrderItemResult> items
) {

    public record OrderItemResult(
            String productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }
}
