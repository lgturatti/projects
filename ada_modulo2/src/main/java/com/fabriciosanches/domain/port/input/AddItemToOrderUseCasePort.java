package com.fabriciosanches.domain.port.input;

public interface AddItemToOrderUseCasePort {

    GetOrderByIdResult execute(AddItemToOrderCommand command);
}
