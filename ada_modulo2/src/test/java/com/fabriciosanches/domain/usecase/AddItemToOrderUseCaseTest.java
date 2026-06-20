package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.model.OrderStatus;
import com.fabriciosanches.domain.port.input.AddItemToOrderCommand;
import com.fabriciosanches.domain.port.input.GetOrderByIdResult;
import com.fabriciosanches.domain.port.output.CatalogClientPort;
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

@DisplayName("AddItemToOrderUseCase - Testes de Unidade")
class AddItemToOrderUseCaseTest {

    private OrderRepositoryPort orderRepository;
    private CatalogClientPort catalogClient;
    private AddItemToOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepositoryPort.class);
        catalogClient = mock(CatalogClientPort.class);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(catalogClient.getProductPrice("product-001")).thenReturn(new BigDecimal("19.99"));
        useCase = new AddItemToOrderUseCase(orderRepository, catalogClient);
    }

    private Order pendingOrder(String orderId) {
        return new Order(orderId, "customer-001", Instant.now(),
                List.of(), OrderStatus.PENDENTE, 0, BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Deve adicionar item ao pedido com sucesso")
    void shouldAddItemToOrderSuccessfully() {
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(pendingOrder("order-001")));

        GetOrderByIdResult result = useCase.execute(
                new AddItemToOrderCommand("order-001", "product-001", "Produto Teste", 2));

        assertNotNull(result);
        assertEquals("order-001", result.orderId());
        assertEquals(1, result.items().size());
        verify(catalogClient).validateStock("product-001", 2);
        verify(catalogClient).getProductPrice("product-001");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando pedido não for encontrado")
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> useCase.execute(new AddItemToOrderCommand("unknown", "p1", "Produto", 1)));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando orderId é nulo")
    void shouldThrowWhenOrderIdIsNull() {
        assertThrows(DomainException.class,
                () -> useCase.execute(new AddItemToOrderCommand(null, "p1", "Produto", 1)));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando productId é nulo")
    void shouldThrowWhenProductIdIsNull() {
        assertThrows(DomainException.class,
                () -> useCase.execute(new AddItemToOrderCommand("order-001", null, "Produto", 1)));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando productName é nulo")
    void shouldThrowWhenProductNameIsNull() {
        assertThrows(DomainException.class,
                () -> useCase.execute(new AddItemToOrderCommand("order-001", "p1", null, 1)));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando quantity é zero ou negativo")
    void shouldThrowWhenQuantityIsInvalid() {
        assertThrows(DomainException.class,
                () -> useCase.execute(new AddItemToOrderCommand("order-001", "p1", "Produto", 0)));
    }

    @Test
    @DisplayName("Deve lançar DomainException ao adicionar item em pedido CONFIRMADO")
    void shouldThrowWhenOrderIsConfirmed() {
        Order confirmed = new Order("order-002", "customer-001", Instant.now(),
                List.of(), OrderStatus.CONFIRMADO, 0, new BigDecimal("10.00"));
        when(orderRepository.findById("order-002")).thenReturn(Optional.of(confirmed));

        assertThrows(DomainException.class,
                () -> useCase.execute(new AddItemToOrderCommand("order-002", "product-001", "Produto", 1)));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando comando é nulo")
    void shouldThrowWhenCommandIsNull() {
        assertThrows(DomainException.class, () -> useCase.execute(null));
    }
}
