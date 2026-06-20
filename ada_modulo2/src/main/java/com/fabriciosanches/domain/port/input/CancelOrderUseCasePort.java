package com.fabriciosanches.domain.port.input;

public interface CancelOrderUseCasePort {

    void execute(CancelOrderCommand command);
}
