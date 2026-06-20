package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.model.OrderStatus;
import com.fabriciosanches.domain.port.input.ConfirmOrderCommand;
import com.fabriciosanches.domain.port.input.GetOrderByIdResult;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fabriciosanches.domain.model.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("ConfirmOrderUseCase - Testes de Unidade")
class ConfirmOrderUseCaseTest {

    private OrderRepositoryPort orderRepository;
    private ConfirmOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepositoryPort.class);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        useCase = new ConfirmOrderUseCase(orderRepository);
    }

    @Test
    @DisplayName("Deve confirmar pedido PENDENTE com sucesso")
    void shouldConfirmPendingOrder() {
        Order order = new Order("order-001", "customer-001", Instant.now(),
                List.of(new OrderItem("p1", "Produto", 1, BigDecimal.TEN)),
                OrderStatus.PENDENTE, 0, BigDecimal.TEN);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

        GetOrderByIdResult result = useCase.execute(new ConfirmOrderCommand("order-001"));

        assertNotNull(result);
        assertEquals("CONFIRMADO", result.status());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve retornar estado atual (idempotência) quando pedido já está CONFIRMADO")
    void shouldBeIdempotentWhenAlreadyConfirmed() {
        Order order = new Order("order-001", "customer-001", Instant.now(),
                List.of(), OrderStatus.CONFIRMADO, 0, BigDecimal.TEN);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

        GetOrderByIdResult result = useCase.execute(new ConfirmOrderCommand("order-001"));

        assertNotNull(result);
        assertEquals("CONFIRMADO", result.status());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando pedido não for encontrado")
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> useCase.execute(new ConfirmOrderCommand("unknown")));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando orderId é nulo")
    void shouldThrowWhenOrderIdIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new ConfirmOrderCommand(null)));
        assertTrue(ex.getMessage().contains("orderId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando orderId está em branco")
    void shouldThrowWhenOrderIdIsBlank() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new ConfirmOrderCommand("")));
        assertTrue(ex.getMessage().contains("orderId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando comando é nulo")
    void shouldThrowWhenCommandIsNull() {
        assertThrows(DomainException.class, () -> useCase.execute(null));
    }
}
