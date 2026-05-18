package payment.service;

import java.util.Random;

public class PaymentService {

    private final Random random = new Random();

    public boolean processPayment(double amount) {

        // Simulate external payment instability
        int result = random.nextInt(10);

        if (result < 3) {
            throw new RuntimeException("Payment gateway unavailable");
        }

        return true;
    }
}