package payment.service;

import payment.domain.PaymentRequest;
import payment.strategy.PaymentStrategy;
import payment.validation.PaymentValidator;

public class PaymentService {
    private final PaymentValidator validator;

    public PaymentService(PaymentValidator validator) {
        this.validator = validator;
    }

    public boolean executePayment(PaymentStrategy strategy, PaymentRequest request) {
        validator.validate(request);
        System.out.println("Selected method: " + strategy.getMethodName());
        return strategy.process(request);
    }
}