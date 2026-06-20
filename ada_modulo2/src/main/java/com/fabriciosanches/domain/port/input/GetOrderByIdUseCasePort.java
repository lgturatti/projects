package com.fabriciosanches.domain.port.input;

public interface GetOrderByIdUseCasePort {

    GetOrderByIdResult execute(GetOrderByIdCommand command);
}
