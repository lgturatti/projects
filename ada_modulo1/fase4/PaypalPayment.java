package payment.strategy;

import payment.domain.PaymentRequest;

public class PaypalPayment implements PaymentStrategy {
    @Override
    public boolean process(PaymentRequest request) {
        System.out.println("Processing PayPal payment: " + request.getAmount());
        return true;
    } 

    @Override
    public String getMethodName() {
        return "PAYPAL";
    }
}