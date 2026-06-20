package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.model.OrderStatus;
import com.fabriciosanches.domain.model.Payment;
import com.fabriciosanches.domain.model.PaymentStatus;
import com.fabriciosanches.domain.port.input.ProcessPaymentCallbackCommand;
import com.fabriciosanches.domain.port.input.ProcessPaymentCallbackResult;
import com.fabriciosanches.domain.port.output.NotificationClientPort;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import com.fabriciosanches.domain.port.output.PaymentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("ProcessPaymentCallbackUseCase - Testes de Unidade")
class ProcessPaymentCallbackUseCaseTest {

    private PaymentRepositoryPort paymentRepository;
    private OrderRepositoryPort orderRepository;
    private NotificationClientPort notificationClient;
    private ProcessPaymentCallbackUseCase useCase;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepositoryPort.class);
        orderRepository = mock(OrderRepositoryPort.class);
        notificationClient = mock(NotificationClientPort.class);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        useCase = new ProcessPaymentCallbackUseCase(paymentRepository, orderRepository, notificationClient);
    }

    private Payment processingPayment() {
        return new Payment("pay-001", "order-001", "CREDIT_CARD",
                PaymentStatus.PROCESSING, Instant.now());
    }

    private Order confirmedOrder() {
        return new Order("order-001", "customer-001", Instant.now(),
                List.of(), OrderStatus.CONFIRMADO, 0, BigDecimal.TEN);
    }

    @Test
    @DisplayName("Deve aprovar pagamento com callback APPROVED")
    void shouldApprovePaymentOnApprovedCallback() {
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(processingPayment()));
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(confirmedOrder()));

        ProcessPaymentCallbackResult result = useCase.execute(
                new ProcessPaymentCallbackCommand("pay-001", "APPROVED"));

        assertNotNull(result);
        assertEquals("pay-001", result.paymentId());
        assertEquals("APPROVED", result.status());
        verify(paymentRepository).save(any(Payment.class));
        verify(notificationClient).notifyOrderApproved("order-001", "customer-001");
    }

    @Test
    @DisplayName("Deve rejeitar pagamento e atualizar pedido com callback REJECTED")
    void shouldRejectPaymentAndUpdateOrderOnRejectedCallback() {
        Order order = new Order("order-001", "customer-001", Instant.now(),
                List.of(), OrderStatus.CONFIRMADO, 0, BigDecimal.TEN);
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(processingPayment()));
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

        ProcessPaymentCallbackResult result = useCase.execute(
                new ProcessPaymentCallbackCommand("pay-001", "REJECTED"));

        assertNotNull(result);
        assertEquals("REJECTED", result.status());
        verify(paymentRepository).save(any(Payment.class));
        verify(orderRepository).save(any(Order.class));
        verify(notificationClient, never()).notifyOrderApproved(any(), any());
    }

    @Test
    @DisplayName("Deve aceitar callback com status em minúsculas (case-insensitive)")
    void shouldAcceptLowercaseCallbackStatus() {
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(processingPayment()));
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(confirmedOrder()));

        ProcessPaymentCallbackResult result = useCase.execute(
                new ProcessPaymentCallbackCommand("pay-001", "approved"));

        assertEquals("APPROVED", result.status());
    }

    @Test
    @DisplayName("Deve ser idempotente quando pagamento já foi processado")
    void shouldBeIdempotentWhenAlreadyProcessed() {
        Payment approved = new Payment("pay-001", "order-001", "CREDIT_CARD",
                PaymentStatus.APPROVED, Instant.now());
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(approved));

        ProcessPaymentCallbackResult result = useCase.execute(
                new ProcessPaymentCallbackCommand("pay-001", "REJECTED"));

        assertEquals("APPROVED", result.status());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar DomainException para callbackStatus inválido")
    void shouldThrowForInvalidCallbackStatus() {
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(processingPayment()));

        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new ProcessPaymentCallbackCommand("pay-001", "PENDING")));
        assertTrue(ex.getMessage().contains("callbackStatus"));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando pagamento não for encontrado")
    void shouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> useCase.execute(new ProcessPaymentCallbackCommand("unknown", "APPROVED")));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando paymentId é nulo")
    void shouldThrowWhenPaymentIdIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new ProcessPaymentCallbackCommand(null, "APPROVED")));
        assertTrue(ex.getMessage().contains("paymentId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando callbackStatus é nulo")
    void shouldThrowWhenCallbackStatusIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new ProcessPaymentCallbackCommand("pay-001", null)));
        assertTrue(ex.getMessage().contains("callbackStatus"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando comando é nulo")
    void shouldThrowWhenCommandIsNull() {
        assertThrows(DomainException.class, () -> useCase.execute(null));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando pedido associado não for encontrado (REJECTED)")
    void shouldThrowWhenOrderNotFoundOnRejection() {
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(processingPayment()));
        when(orderRepository.findById("order-001")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> useCase.execute(new ProcessPaymentCallbackCommand("pay-001", "REJECTED")));
    }
}
