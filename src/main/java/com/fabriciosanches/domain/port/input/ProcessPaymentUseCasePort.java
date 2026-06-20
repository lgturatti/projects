package com.fabriciosanches.domain.port.input;

public interface ProcessPaymentUseCasePort {

    ProcessPaymentResult execute(ProcessPaymentCommand command);
}
