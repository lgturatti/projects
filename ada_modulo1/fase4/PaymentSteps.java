package bdd;

import io.cucumber.java.en.*;
import payment.domain.PaymentRequest; 
import payment.service.PaymentService;
import payment.strategy.CreditCardPayment; 
import payment.validation.PaymentValidator;
import static org.junit.jupiter.api.Assertions.*;

public class PaymentSteps {
    private PaymentRequest request; 
    private boolean paymentResult;

    @Given("the payment amount is {double}") 
    public void paymentAmount(double amount) { 
        request = new PaymentRequest(amount, "bdd@test.com");
    }

    @When("the customer pays using credit card")
    public void customerPays() {
        PaymentService service = new PaymentService(new PaymentValidator());
        paymentResult = service.executePayment(new CreditCardPayment(), request);
    }

    @Then("the payment should be approved")
    public void paymentApproved() {
        assertTrue(paymentResult);
    }
}