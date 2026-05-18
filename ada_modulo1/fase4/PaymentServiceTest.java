package payment.service;

import org.junit.jupiter.api.Test;
import payment.domain.PaymentRequest;
import payment.strategy.CreditCardPayment;
import payment.validation.PaymentValidator;
import static org.junit.jupiter.api.Assertions.*;

public class PaymentServiceTest {

    @Test
    void shouldProcessPaymentSuccessfully() {
        PaymentValidator validator = new PaymentValidator();
        PaymentService service = new PaymentService(validator);
        PaymentRequest request = new PaymentRequest(150.0, "user@test.com");

        boolean result = service.executePayment(new CreditCardPayment(), request);
        assertTrue(result);
    }

    @Test
    void shouldRejectInvalidAmount() {
        PaymentValidator validator = new PaymentValidator();
        PaymentService service = new PaymentService(validator);
        PaymentRequest request = new PaymentRequest(-10.0, "user@test.com");

        assertThrows(IllegalArgumentException.class, () -> {
            service.executePayment(new CreditCardPayment(), request);
        });
    }

    @Test
    void shouldRejectBlankEmail() {
        PaymentValidator validator = new PaymentValidator();
        PaymentService service = new PaymentService(validator);
        PaymentRequest request = new PaymentRequest(100.0, "");

        assertThrows(IllegalArgumentException.class, () -> {
            service.executePayment(new CreditCardPayment(), request);
        });
    }
}