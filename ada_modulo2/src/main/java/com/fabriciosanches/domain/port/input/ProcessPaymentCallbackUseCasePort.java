package com.fabriciosanches.domain.port.input;

public interface ProcessPaymentCallbackUseCasePort {

    ProcessPaymentCallbackResult execute(ProcessPaymentCallbackCommand command);
}
