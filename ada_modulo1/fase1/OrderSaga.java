package sales.saga;

import catalog.domain.Event;
import payment.service.PaymentService;
import shared.resilience.CircuitBreaker;

public class OrderSaga {

    private final PaymentService paymentService;
    private final CircuitBreaker circuitBreaker;

    public OrderSaga(PaymentService paymentService,
                     CircuitBreaker circuitBreaker) {

        this.paymentService = paymentService;
        this.circuitBreaker = circuitBreaker;
    }

    public void execute(Event event, int quantity) {

        System.out.println("Starting SAGA transaction...");

        boolean reserved = event.reserveTickets(quantity);

        if (!reserved) {
            System.out.println("Insufficient inventory.");
            return;
        }

        System.out.println("Inventory reserved.");

        try {

            if (!circuitBreaker.allowRequest()) {
                throw new RuntimeException("Circuit breaker OPEN");
            }

            paymentService.processPayment(quantity * 100);

            circuitBreaker.recordSuccess();

            System.out.println("Payment approved.");
            System.out.println("Order confirmed.");

        } catch (Exception ex) {

            circuitBreaker.recordFailure();

            System.out.println("Payment failed.");
            System.out.println("Executing compensation...");

            event.releaseTickets(quantity);

            System.out.println("Inventory restored.");
        }
    }
}