package payment.domain;

public class PaymentRequest {
    private final double amount;
    private final String customerEmail;

    public PaymentRequest(double amount, String customerEmail) {
        this.amount = amount;
        this.customerEmail = customerEmail;
    }

    public double getAmount() {
        return amount;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }
}