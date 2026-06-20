package com.fabriciosanches.domain.port.input;

public interface CreateOrderUseCasePort {

    CreateOrderResult execute(CreateOrderCommand command);
}
