package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.model.OrderStatus;
import com.fabriciosanches.domain.port.input.CancelOrderCommand;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
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

@DisplayName("CancelOrderUseCase - Testes de Unidade")
class CancelOrderUseCaseTest {

    private OrderRepositoryPort orderRepository;
    private CancelOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepositoryPort.class);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        useCase = new CancelOrderUseCase(orderRepository);
    }

    @Test
    @DisplayName("Deve cancelar pedido PENDENTE com sucesso")
    void shouldCancelPendingOrder() {
        Order order = new Order("order-001", "customer-001", Instant.now(),
                List.of(), OrderStatus.PENDENTE, 0, BigDecimal.ZERO);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

        assertDoesNotThrow(() -> useCase.execute(new CancelOrderCommand("order-001")));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve cancelar pedido CONFIRMADO com sucesso")
    void shouldCancelConfirmedOrder() {
        Order order = new Order("order-001", "customer-001", Instant.now(),
                List.of(), OrderStatus.CONFIRMADO, 0, BigDecimal.TEN);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

        assertDoesNotThrow(() -> useCase.execute(new CancelOrderCommand("order-001")));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve ser idempotente quando pedido já está CANCELADO")
    void shouldBeIdempotentWhenAlreadyCancelled() {
        Order order = new Order("order-001", "customer-001", Instant.now(),
                List.of(), OrderStatus.CANCELADO, 0, BigDecimal.ZERO);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

        assertDoesNotThrow(() -> useCase.execute(new CancelOrderCommand("order-001")));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando pedido não for encontrado")
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> useCase.execute(new CancelOrderCommand("unknown")));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando orderId é nulo")
    void shouldThrowWhenOrderIdIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new CancelOrderCommand(null)));
        assertTrue(ex.getMessage().contains("orderId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando orderId está em branco")
    void shouldThrowWhenOrderIdIsBlank() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new CancelOrderCommand("  ")));
        assertTrue(ex.getMessage().contains("orderId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando comando é nulo")
    void shouldThrowWhenCommandIsNull() {
        assertThrows(DomainException.class, () -> useCase.execute(null));
    }
}
