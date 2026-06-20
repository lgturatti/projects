package com.fabriciosanches.domain.port.input;

import java.util.List;

public record ListOrdersByCustomerResult(List<GetOrderByIdResult> orders) {
}
