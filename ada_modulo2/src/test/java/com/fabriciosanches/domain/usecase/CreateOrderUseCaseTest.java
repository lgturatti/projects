package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.port.input.CreateOrderCommand;
import com.fabriciosanches.domain.port.input.CreateOrderResult;
import com.fabriciosanches.domain.port.output.CustomerClientPort;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("CreateOrderUseCase - Testes de Unidade")
class CreateOrderUseCaseTest {

    private OrderRepositoryPort orderRepository;
    private CustomerClientPort customerClient;
    private CreateOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepositoryPort.class);
        customerClient = mock(CustomerClientPort.class);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        useCase = new CreateOrderUseCase(orderRepository, customerClient);
    }

    @Test
    @DisplayName("Deve criar pedido e retornar um orderId quando o comando é válido")
    void shouldReturnOrderIdWhenCommandIsValid() {
        CreateOrderResult result = useCase.execute(new CreateOrderCommand("customer-001"));

        assertNotNull(result);
        assertNotNull(result.orderId());
        assertFalse(result.orderId().isBlank());
        verify(customerClient).validateCustomer("customer-001");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando o comando é nulo")
    void shouldThrowWhenCommandIsNull() {
        DomainException ex = assertThrows(DomainException.class, () -> useCase.execute(null));
        assertTrue(ex.getMessage().contains("customerId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando o customerId é nulo")
    void shouldThrowWhenCustomerIdIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new CreateOrderCommand(null)));
        assertTrue(ex.getMessage().contains("customerId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando o customerId está em branco")
    void shouldThrowWhenCustomerIdIsBlank() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new CreateOrderCommand("   ")));
        assertTrue(ex.getMessage().contains("customerId"));
    }

    @Test
    @DisplayName("Deve gerar orderId único a cada execução")
    void shouldGenerateUniqueOrderIdOnEachExecution() {
        CreateOrderResult first = useCase.execute(new CreateOrderCommand("customer-001"));
        CreateOrderResult second = useCase.execute(new CreateOrderCommand("customer-001"));

        assertNotEquals(first.orderId(), second.orderId());
    }
}
