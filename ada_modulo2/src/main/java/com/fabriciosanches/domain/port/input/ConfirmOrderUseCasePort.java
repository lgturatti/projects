package com.fabriciosanches.domain.port.input;

public interface ConfirmOrderUseCasePort {

    GetOrderByIdResult execute(ConfirmOrderCommand command);
}
