package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.model.OrderItem;
import com.fabriciosanches.domain.model.OrderStatus;
import com.fabriciosanches.domain.model.Payment;
import com.fabriciosanches.domain.port.input.ProcessPaymentCommand;
import com.fabriciosanches.domain.port.input.ProcessPaymentResult;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import com.fabriciosanches.domain.port.output.PaymentGatewayClientPort;
import com.fabriciosanches.domain.port.output.PaymentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("ProcessPaymentUseCase - Testes de Unidade")
class ProcessPaymentUseCaseTest {

    private OrderRepositoryPort orderRepository;
    private PaymentRepositoryPort paymentRepository;
    private PaymentGatewayClientPort paymentGatewayClient;
    private ProcessPaymentUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepositoryPort.class);
        paymentRepository = mock(PaymentRepositoryPort.class);
        paymentGatewayClient = mock(PaymentGatewayClientPort.class);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentGatewayClient.charge(any(), any(), any()))
                .thenReturn(new PaymentGatewayClientPort.PaymentGatewayResult("txn-001", "APPROVED"));
        useCase = new ProcessPaymentUseCase(orderRepository, paymentRepository, paymentGatewayClient);
    }

    private Order confirmedOrder(String orderId) {
        return new Order(orderId, "customer-001",
                java.time.Instant.now(),
                List.of(new OrderItem("p1", "Produto", 1, new BigDecimal("10.00"))),
                OrderStatus.CONFIRMADO, 0, new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("Deve processar pagamento e retornar paymentId quando o comando é válido")
    void shouldReturnPaymentResultWhenCommandIsValid() {
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(confirmedOrder("order-001")));

        ProcessPaymentResult result = useCase.execute(new ProcessPaymentCommand("order-001", "pix"));

        assertNotNull(result);
        assertNotNull(result.paymentId());
        assertFalse(result.paymentId().isBlank());
        assertNotNull(result.status());
        verify(paymentGatewayClient).charge(eq("order-001"), any(), eq("pix"));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando o comando é nulo")
    void shouldThrowWhenCommandIsNull() {
        DomainException ex = assertThrows(DomainException.class, () -> useCase.execute(null));
        assertTrue(ex.getMessage().contains("orderId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando o orderId é nulo")
    void shouldThrowWhenOrderIdIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new ProcessPaymentCommand(null, "pix")));
        assertTrue(ex.getMessage().contains("orderId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando o orderId está em branco")
    void shouldThrowWhenOrderIdIsBlank() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new ProcessPaymentCommand("  ", "pix")));
        assertTrue(ex.getMessage().contains("orderId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando o paymentMethod é nulo")
    void shouldThrowWhenPaymentMethodIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new ProcessPaymentCommand("order-001", null)));
        assertTrue(ex.getMessage().contains("paymentMethod"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando o paymentMethod está em branco")
    void shouldThrowWhenPaymentMethodIsBlank() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new ProcessPaymentCommand("order-001", "")));
        assertTrue(ex.getMessage().contains("paymentMethod"));
    }

    @Test
    @DisplayName("Deve gerar paymentId único a cada execução")
    void shouldGenerateUniquePaymentIdOnEachExecution() {
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(confirmedOrder("order-001")));

        ProcessPaymentResult first = useCase.execute(new ProcessPaymentCommand("order-001", "pix"));
        ProcessPaymentResult second = useCase.execute(new ProcessPaymentCommand("order-001", "pix"));

        assertNotEquals(first.paymentId(), second.paymentId());
    }

    @Test
    @DisplayName("Deve lançar DomainException quando o pedido está PENDENTE")
    void shouldThrowWhenOrderIsPending() {
        Order pendingOrder = new Order("order-002", "customer-001",
                java.time.Instant.now(), List.of(), OrderStatus.PENDENTE, 0, BigDecimal.ZERO);
        when(orderRepository.findById("order-002")).thenReturn(Optional.of(pendingOrder));

        assertThrows(DomainException.class,
                () -> useCase.execute(new ProcessPaymentCommand("order-002", "pix")));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando o pedido está CANCELADO")
    void shouldThrowWhenOrderIsCancelled() {
        Order cancelledOrder = new Order("order-003", "customer-001",
                java.time.Instant.now(), List.of(), OrderStatus.CANCELADO, 0, BigDecimal.ZERO);
        when(orderRepository.findById("order-003")).thenReturn(Optional.of(cancelledOrder));

        assertThrows(DomainException.class,
                () -> useCase.execute(new ProcessPaymentCommand("order-003", "pix")));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando o gateway de pagamento rejeita a cobrança")
    void shouldThrowWhenPaymentGatewayFails() {
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(confirmedOrder("order-001")));
        when(paymentGatewayClient.charge(any(), any(), any()))
                .thenThrow(new DomainException("Gateway de pagamento indisponível."));

        assertThrows(DomainException.class,
                () -> useCase.execute(new ProcessPaymentCommand("order-001", "pix")));
        verify(paymentRepository, never()).save(any());
    }
}
