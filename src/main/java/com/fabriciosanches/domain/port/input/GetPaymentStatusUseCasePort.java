package com.fabriciosanches.domain.port.input;

public interface GetPaymentStatusUseCasePort {

    GetPaymentStatusResult execute(GetPaymentStatusCommand command);
}
