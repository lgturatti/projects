package payment;

import payment.domain.PaymentRequest;
import payment.service.PaymentService; 
import payment.strategy.CreditCardPayment; 
import payment.strategy.PaymentStrategy; 
import payment.validation.PaymentValidator; 

public class Main { 
    public static void main(String[] args) {
        PaymentRequest request = new PaymentRequest(250.0, "user@eventmaster.com");
        
        PaymentValidator validator = new PaymentValidator();
        PaymentService paymentService = new PaymentService(validator);
        PaymentStrategy strategy = new CreditCardPayment();

        boolean success = paymentService.executePayment(strategy, request);
        System.out.println("Payment success: " + success);
    } 
} 