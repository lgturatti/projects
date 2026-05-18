package payment.validation;

import payment.domain.PaymentRequest;

public class PaymentValidator {
    public void validate(PaymentRequest request) { 
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid payment amount");
        } 
        if (request.getCustomerEmail() == null || request.getCustomerEmail().isBlank()) {
            throw new IllegalArgumentException("Customer email required");
        } 
    } 
}