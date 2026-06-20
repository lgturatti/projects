package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.model.OrderItem;
import com.fabriciosanches.domain.model.OrderStatus;
import com.fabriciosanches.domain.port.input.RemoveItemFromOrderCommand;
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

@DisplayName("RemoveItemFromOrderUseCase - Testes de Unidade")
class RemoveItemFromOrderUseCaseTest {

    private OrderRepositoryPort orderRepository;
    private RemoveItemFromOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepositoryPort.class);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        useCase = new RemoveItemFromOrderUseCase(orderRepository);
    }

    private Order pendingOrderWithItem(String orderId) {
        Order order = new Order(orderId, "customer-001", Instant.now(),
                List.of(new OrderItem("product-001", "Produto", 1, new BigDecimal("10.00"))),
                OrderStatus.PENDENTE, 0, BigDecimal.ZERO);
        return order;
    }

    @Test
    @DisplayName("Deve remover item do pedido com sucesso")
    void shouldRemoveItemSuccessfully() {
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(pendingOrderWithItem("order-001")));

        assertDoesNotThrow(() -> useCase.execute(new RemoveItemFromOrderCommand("order-001", "product-001")));

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando pedido não for encontrado")
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> useCase.execute(new RemoveItemFromOrderCommand("unknown", "product-001")));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando item não for encontrado no pedido")
    void shouldThrowWhenItemNotFound() {
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(pendingOrderWithItem("order-001")));

        assertThrows(DomainException.class,
                () -> useCase.execute(new RemoveItemFromOrderCommand("order-001", "non-existent-product")));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando orderId é nulo")
    void shouldThrowWhenOrderIdIsNull() {
        assertThrows(DomainException.class,
                () -> useCase.execute(new RemoveItemFromOrderCommand(null, "product-001")));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando productId é nulo")
    void shouldThrowWhenProductIdIsNull() {
        assertThrows(DomainException.class,
                () -> useCase.execute(new RemoveItemFromOrderCommand("order-001", null)));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando comando é nulo")
    void shouldThrowWhenCommandIsNull() {
        assertThrows(DomainException.class, () -> useCase.execute(null));
    }
}
