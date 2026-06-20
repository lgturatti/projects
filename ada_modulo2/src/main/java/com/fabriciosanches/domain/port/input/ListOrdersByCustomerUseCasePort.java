package com.fabriciosanches.domain.port.input;

public interface ListOrdersByCustomerUseCasePort {

    ListOrdersByCustomerResult execute(ListOrdersByCustomerCommand command);
}
