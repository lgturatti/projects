package payment.strategy;

import payment.domain.PaymentRequest;

public class PixPayment implements PaymentStrategy {
    @Override
    public boolean process(PaymentRequest request) {
        System.out.println("Processing PIX payment: " + request.getAmount());
        return true;
    }

    @Override
    public String getMethodName() {
        return "PIX";
    } 
}