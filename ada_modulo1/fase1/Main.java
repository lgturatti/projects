import catalog.domain.Event;
import payment.service.PaymentService;
import sales.saga.OrderSaga;
import shared.resilience.CircuitBreaker;

import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        Event concert = new Event(
                1L,
                "Rock Festival",
                "Annual music festival",
                100.0,
                LocalDateTime.now(),
                10
        );

        PaymentService paymentService = new PaymentService();

        CircuitBreaker circuitBreaker = new CircuitBreaker();

        OrderSaga saga =
                new OrderSaga(paymentService, circuitBreaker);

        for (int i = 0; i < 6; i++) {

            System.out.println("\nPURCHASE ATTEMPT #" + (i + 1));

            saga.execute(concert, 2);

            System.out.println(
                    "Remaining tickets: "
                    + concert.getAvailableTickets()
            );

            System.out.println(
                    "Circuit breaker OPEN? "
                    + circuitBreaker.isOpen()
            );
        }
    }
}