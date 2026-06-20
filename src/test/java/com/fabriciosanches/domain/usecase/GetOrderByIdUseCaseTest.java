package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.model.OrderStatus;
import com.fabriciosanches.domain.port.input.GetOrderByIdCommand;
import com.fabriciosanches.domain.port.input.GetOrderByIdResult;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GetOrderByIdUseCase - Testes de Unidade")
class GetOrderByIdUseCaseTest {

    private OrderRepositoryPort orderRepository;
    private GetOrderByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepositoryPort.class);
        useCase = new GetOrderByIdUseCase(orderRepository);
    }

    private Order buildOrder(String id) {
        return new Order(id, "customer-001", Instant.now(), List.of(),
                OrderStatus.PENDENTE, 0, BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Deve retornar detalhes do pedido quando encontrado")
    void shouldReturnOrderWhenFound() {
        Order order = buildOrder("order-001");
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

        GetOrderByIdResult result = useCase.execute(new GetOrderByIdCommand("order-001"));

        assertNotNull(result);
        assertEquals("order-001", result.orderId());
        assertEquals("customer-001", result.customerId());
        assertEquals("PENDENTE", result.status());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando pedido não for encontrado")
    void shouldThrowResourceNotFoundWhenOrderNotFound() {
        when(orderRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> useCase.execute(new GetOrderByIdCommand("unknown")));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando orderId é nulo")
    void shouldThrowDomainExceptionWhenOrderIdIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new GetOrderByIdCommand(null)));
        assertTrue(ex.getMessage().contains("orderId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando orderId está em branco")
    void shouldThrowDomainExceptionWhenOrderIdIsBlank() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new GetOrderByIdCommand("  ")));
        assertTrue(ex.getMessage().contains("orderId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando comando é nulo")
    void shouldThrowDomainExceptionWhenCommandIsNull() {
        assertThrows(DomainException.class, () -> useCase.execute(null));
    }
}
