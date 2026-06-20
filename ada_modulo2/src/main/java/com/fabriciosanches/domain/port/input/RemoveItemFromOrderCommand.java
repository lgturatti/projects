package com.fabriciosanches.domain.port.input;

public record RemoveItemFromOrderCommand(String orderId, String productId) {
}
