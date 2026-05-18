package payment.strategy;

import payment.domain.PaymentRequest;

public interface PaymentStrategy {
    boolean process(PaymentRequest request);
    String getMethodName();
}