package com.fabriciosanches.domain.port.input;

public interface RemoveItemFromOrderUseCasePort {

    void execute(RemoveItemFromOrderCommand command);
}
