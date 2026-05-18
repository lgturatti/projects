package payment.strategy;

import payment.domain.PaymentRequest;

public class CreditCardPayment implements PaymentStrategy {
    @Override
    public boolean process(PaymentRequest request) {
        System.out.println("Processing credit card payment: " + request.getAmount());
        return true; 
    }

    @Override
    public String getMethodName() {
        return "CREDIT_CARD";
    }
}