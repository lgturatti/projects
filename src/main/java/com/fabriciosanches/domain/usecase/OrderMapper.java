package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.port.input.GetOrderByIdResult;

import java.util.List;

final class OrderMapper {

    private OrderMapper() {
    }

    static GetOrderByIdResult toResult(Order order) {
        List<GetOrderByIdResult.OrderItemResult> itemResults = order.getItems().stream()
                .map(item -> new GetOrderByIdResult.OrderItemResult(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()))
                .toList();
        return new GetOrderByIdResult(
                order.getId(),
                order.getCustomerId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getPaymentFailureCount(),
                order.getCreatedAt(),
                itemResults);
    }
}
