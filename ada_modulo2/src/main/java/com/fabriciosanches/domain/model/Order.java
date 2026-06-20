package com.fabriciosanches.domain.model;

import com.fabriciosanches.domain.exception.DomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Order {

    private static final int MAX_PAYMENT_FAILURES = 3;

    private final String id;
    private final String customerId;
    private final Instant createdAt;
    private final List<OrderItem> items;
    private OrderStatus status;
    private int paymentFailureCount;
    private BigDecimal totalAmount;

    public Order(String customerId) {
        Objects.requireNonNull(customerId, "customerId não pode ser nulo");
        this.id = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.createdAt = Instant.now();
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDENTE;
        this.paymentFailureCount = 0;
        this.totalAmount = BigDecimal.ZERO;
    }

    public Order(String id, String customerId, Instant createdAt, List<OrderItem> items,
                 OrderStatus status, int paymentFailureCount, BigDecimal totalAmount) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        Objects.requireNonNull(customerId, "customerId não pode ser nulo");
        Objects.requireNonNull(createdAt, "createdAt não pode ser nulo");
        Objects.requireNonNull(items, "items não pode ser nulo");
        Objects.requireNonNull(status, "status não pode ser nulo");
        Objects.requireNonNull(totalAmount, "totalAmount não pode ser nulo");
        this.id = id;
        this.customerId = customerId;
        this.createdAt = createdAt;
        this.items = new ArrayList<>(items);
        this.status = status;
        this.paymentFailureCount = paymentFailureCount;
        this.totalAmount = totalAmount;
    }

    public void addItem(OrderItem item) {
        Objects.requireNonNull(item, "item não pode ser nulo");
        if (!status.canAddItems()) {
            throw new DomainException(
                    String.format("Não é possível adicionar itens a um pedido com status '%s'.", status));
        }
        items.add(item);
    }

    public void confirmOrder() {
        if (!status.canConfirm()) {
            throw new DomainException(
                    String.format("Não é possível confirmar um pedido com status '%s'.", status));
        }
        if (items.isEmpty()) {
            throw new DomainException("Não é possível confirmar um pedido sem itens.");
        }
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.status = OrderStatus.CONFIRMADO;
    }

    public void removeItem(String productId) {
        Objects.requireNonNull(productId, "productId não pode ser nulo");
        if (!status.canAddItems()) {
            throw new DomainException(
                    String.format("Não é possível remover itens de um pedido com status '%s'.", status));
        }
        boolean removed = items.removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            throw new DomainException(
                    String.format("Item com productId '%s' não encontrado no pedido.", productId));
        }
    }

    public void cancel() {
        if (!status.canCancel()) {
            throw new DomainException(
                    String.format("Não é possível cancelar um pedido com status '%s'.", status));
        }
        this.status = OrderStatus.CANCELADO;
    }

    public void applyPaymentFailure() {
        if (!status.canApplyPaymentFailure()) {
            throw new DomainException(
                    String.format("Não é possível registrar falha de pagamento em um pedido com status '%s'.", status));
        }
        paymentFailureCount++;
        if (paymentFailureCount >= MAX_PAYMENT_FAILURES) {
            this.status = OrderStatus.CANCELADO;
        }
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getPaymentFailureCount() {
        return paymentFailureCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
