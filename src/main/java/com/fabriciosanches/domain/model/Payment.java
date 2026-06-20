package com.fabriciosanches.domain.model;

import com.fabriciosanches.domain.exception.DomainException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Payment {

    private final String id;
    private final String orderId;
    private final String paymentMethod;
    private PaymentStatus status;
    private final Instant createdAt;

    public Payment(String orderId, String paymentMethod) {
        Objects.requireNonNull(orderId, "orderId não pode ser nulo");
        Objects.requireNonNull(paymentMethod, "paymentMethod não pode ser nulo");
        this.id = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PROCESSING;
        this.createdAt = Instant.now();
    }

    public Payment(String id, String orderId, String paymentMethod, PaymentStatus status, Instant createdAt) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        Objects.requireNonNull(orderId, "orderId não pode ser nulo");
        Objects.requireNonNull(paymentMethod, "paymentMethod não pode ser nulo");
        Objects.requireNonNull(status, "status não pode ser nulo");
        Objects.requireNonNull(createdAt, "createdAt não pode ser nulo");
        this.id = id;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void approve() {
        if (status != PaymentStatus.PROCESSING) {
            throw new DomainException(
                    String.format("Não é possível aprovar um pagamento com status '%s'.", status));
        }
        this.status = PaymentStatus.APPROVED;
    }

    public void reject() {
        if (status != PaymentStatus.PROCESSING) {
            throw new DomainException(
                    String.format("Não é possível rejeitar um pagamento com status '%s'.", status));
        }
        this.status = PaymentStatus.REJECTED;
    }

    public boolean isAlreadyProcessed() {
        return status != PaymentStatus.PROCESSING;
    }

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
