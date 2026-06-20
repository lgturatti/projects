package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.model.OrderStatus;
import com.fabriciosanches.domain.port.input.ListOrdersByCustomerCommand;
import com.fabriciosanches.domain.port.input.ListOrdersByCustomerResult;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ListOrdersByCustomerUseCase - Testes de Unidade")
class ListOrdersByCustomerUseCaseTest {

    private OrderRepositoryPort orderRepository;
    private ListOrdersByCustomerUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepositoryPort.class);
        useCase = new ListOrdersByCustomerUseCase(orderRepository);
    }

    @Test
    @DisplayName("Deve retornar lista de pedidos do cliente")
    void shouldReturnOrdersForCustomer() {
        Order order = new Order("order-001", "customer-001", Instant.now(),
                List.of(), OrderStatus.PENDENTE, 0, BigDecimal.ZERO);
        when(orderRepository.findByCustomerId("customer-001")).thenReturn(List.of(order));

        ListOrdersByCustomerResult result = useCase.execute(new ListOrdersByCustomerCommand("customer-001"));

        assertNotNull(result);
        assertEquals(1, result.orders().size());
        assertEquals("order-001", result.orders().get(0).orderId());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando cliente não possui pedidos")
    void shouldReturnEmptyListWhenNoOrders() {
        when(orderRepository.findByCustomerId("customer-empty")).thenReturn(List.of());

        ListOrdersByCustomerResult result = useCase.execute(new ListOrdersByCustomerCommand("customer-empty"));

        assertNotNull(result);
        assertTrue(result.orders().isEmpty());
    }

    @Test
    @DisplayName("Deve lançar DomainException quando customerId é nulo")
    void shouldThrowWhenCustomerIdIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new ListOrdersByCustomerCommand(null)));
        assertTrue(ex.getMessage().contains("customerId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando customerId está em branco")
    void shouldThrowWhenCustomerIdIsBlank() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new ListOrdersByCustomerCommand("")));
        assertTrue(ex.getMessage().contains("customerId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando comando é nulo")
    void shouldThrowWhenCommandIsNull() {
        assertThrows(DomainException.class, () -> useCase.execute(null));
    }
}
