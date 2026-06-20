package com.fabriciosanches.domain.port.input;

public record AddItemToOrderCommand(
        String orderId,
        String productId,
        String productName,
        int quantity
) {
}
