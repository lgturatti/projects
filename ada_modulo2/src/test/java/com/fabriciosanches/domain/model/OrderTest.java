package com.fabriciosanches.domain.model;

import com.fabriciosanches.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order - Testes de domínio")
class OrderTest {

    private static final String CUSTOMER_ID = "customer-001";
    private static final String PRODUCT_ID = "product-001";
    private static final String PRODUCT_NAME = "Produto Teste";

    private OrderItem buildItem(int quantity, String price) {
        return new OrderItem(PRODUCT_ID, PRODUCT_NAME, quantity, new BigDecimal(price));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Criação do pedido
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve criar pedido em status PENDENTE com lista de itens vazia")
    void shouldCreateOrderWithPendingStatus() {
        Order order = new Order(CUSTOMER_ID);

        assertNotNull(order.getId());
        assertEquals(CUSTOMER_ID, order.getCustomerId());
        assertEquals(OrderStatus.PENDENTE, order.getStatus());
        assertTrue(order.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, order.getTotalAmount());
        assertEquals(0, order.getPaymentFailureCount());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar pedido com customerId nulo")
    void shouldThrowWhenCustomerIdIsNull() {
        assertThrows(NullPointerException.class, () -> new Order(null));
    }

    @Test
    @DisplayName("Deve reconstruir pedido via construtor completo")
    void shouldReconstructOrderViaFullConstructor() {
        java.time.Instant now = java.time.Instant.now();
        List<OrderItem> items = List.of(buildItem(1, "50.00"));
        Order order = new Order("id-001", CUSTOMER_ID, now, items,
                OrderStatus.CONFIRMADO, 1, new BigDecimal("50.00"));

        assertEquals("id-001", order.getId());
        assertEquals(CUSTOMER_ID, order.getCustomerId());
        assertEquals(now, order.getCreatedAt());
        assertEquals(1, order.getItems().size());
        assertEquals(OrderStatus.CONFIRMADO, order.getStatus());
        assertEquals(1, order.getPaymentFailureCount());
        assertEquals(new BigDecimal("50.00"), order.getTotalAmount());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // addItem
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addItem()")
    class AddItemTests {

        @Test
        @DisplayName("Deve adicionar item com sucesso quando pedido está PENDENTE")
        void shouldAddItemWhenPending() {
            Order order = new Order(CUSTOMER_ID);
            OrderItem item = buildItem(2, "50.00");

            order.addItem(item);

            assertEquals(1, order.getItems().size());
            assertEquals(item, order.getItems().get(0));
        }

        @Test
        @DisplayName("Deve lançar DomainException ao adicionar item em pedido CONFIRMADO")
        void shouldThrowWhenAddingItemToConfirmedOrder() {
            Order order = new Order(CUSTOMER_ID);
            order.addItem(buildItem(1, "10.00"));
            order.confirmOrder();

            assertThrows(DomainException.class, () -> order.addItem(buildItem(1, "5.00")));
        }

        @Test
        @DisplayName("Deve lançar DomainException ao adicionar item em pedido CANCELADO")
        void shouldThrowWhenAddingItemToCancelledOrder() {
            Order order = new Order(CUSTOMER_ID);
            order.addItem(buildItem(1, "10.00"));
            order.confirmOrder();
            applyPaymentFailuresTimes(order, 3);

            assertThrows(DomainException.class, () -> order.addItem(buildItem(1, "5.00")));
        }

        @Test
        @DisplayName("Deve lançar NullPointerException ao adicionar item nulo")
        void shouldThrowWhenItemIsNull() {
            Order order = new Order(CUSTOMER_ID);

            assertThrows(NullPointerException.class, () -> order.addItem(null));
        }

        @Test
        @DisplayName("Deve retornar lista imutável de itens")
        void shouldReturnUnmodifiableItemsList() {
            Order order = new Order(CUSTOMER_ID);
            order.addItem(buildItem(1, "10.00"));

            assertThrows(UnsupportedOperationException.class,
                    () -> order.getItems().add(buildItem(1, "5.00")));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // confirmOrder
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("confirmOrder()")
    class ConfirmOrderTests {

        @Test
        @DisplayName("Deve confirmar pedido e calcular total corretamente")
        void shouldConfirmOrderAndCalculateTotal() {
            Order order = new Order(CUSTOMER_ID);
            order.addItem(new OrderItem("p1", "Produto 1", 2, new BigDecimal("10.00")));
            order.addItem(new OrderItem("p2", "Produto 2", 3, new BigDecimal("5.00")));

            order.confirmOrder();

            assertEquals(OrderStatus.CONFIRMADO, order.getStatus());
            assertEquals(new BigDecimal("35.00"), order.getTotalAmount());
        }

        @Test
        @DisplayName("Deve lançar DomainException ao confirmar pedido sem itens")
        void shouldThrowWhenConfirmingEmptyOrder() {
            Order order = new Order(CUSTOMER_ID);

            DomainException ex = assertThrows(DomainException.class, order::confirmOrder);
            assertTrue(ex.getMessage().contains("sem itens"));
        }

        @Test
        @DisplayName("Deve lançar DomainException ao confirmar pedido já CONFIRMADO")
        void shouldThrowWhenConfirmingAlreadyConfirmedOrder() {
            Order order = new Order(CUSTOMER_ID);
            order.addItem(buildItem(1, "10.00"));
            order.confirmOrder();

            assertThrows(DomainException.class, order::confirmOrder);
        }

        @Test
        @DisplayName("Deve lançar DomainException ao confirmar pedido CANCELADO")
        void shouldThrowWhenConfirmingCancelledOrder() {
            Order order = new Order(CUSTOMER_ID);
            order.addItem(buildItem(1, "10.00"));
            order.confirmOrder();
            applyPaymentFailuresTimes(order, 3);

            assertThrows(DomainException.class, order::confirmOrder);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // removeItem
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("removeItem()")
    class RemoveItemTests {

        @Test
        @DisplayName("Deve remover item existente de pedido PENDENTE")
        void shouldRemoveExistingItemFromPendingOrder() {
            Order order = new Order(CUSTOMER_ID);
            order.addItem(buildItem(1, "10.00"));

            order.removeItem(PRODUCT_ID);

            assertTrue(order.getItems().isEmpty());
        }

        @Test
        @DisplayName("Deve lançar DomainException ao remover item de pedido CONFIRMADO")
        void shouldThrowWhenRemovingItemFromConfirmedOrder() {
            Order order = new Order(CUSTOMER_ID);
            order.addItem(buildItem(1, "10.00"));
            order.confirmOrder();

            assertThrows(DomainException.class, () -> order.removeItem(PRODUCT_ID));
        }

        @Test
        @DisplayName("Deve lançar DomainException ao remover item inexistente")
        void shouldThrowWhenItemNotFound() {
            Order order = new Order(CUSTOMER_ID);
            order.addItem(buildItem(1, "10.00"));

            assertThrows(DomainException.class, () -> order.removeItem("non-existent-product"));
        }

        @Test
        @DisplayName("Deve lançar NullPointerException ao remover com productId nulo")
        void shouldThrowWhenProductIdIsNull() {
            Order order = new Order(CUSTOMER_ID);

            assertThrows(NullPointerException.class, () -> order.removeItem(null));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // cancel
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancel()")
    class CancelTests {

        @Test
        @DisplayName("Deve cancelar pedido PENDENTE")
        void shouldCancelPendingOrder() {
            Order order = new Order(CUSTOMER_ID);

            order.cancel();

            assertEquals(OrderStatus.CANCELADO, order.getStatus());
        }

        @Test
        @DisplayName("Deve cancelar pedido CONFIRMADO")
        void shouldCancelConfirmedOrder() {
            Order order = new Order(CUSTOMER_ID);
            order.addItem(buildItem(1, "10.00"));
            order.confirmOrder();

            order.cancel();

            assertEquals(OrderStatus.CANCELADO, order.getStatus());
        }

        @Test
        @DisplayName("Deve lançar DomainException ao cancelar pedido já CANCELADO")
        void shouldThrowWhenCancellingAlreadyCancelledOrder() {
            Order order = new Order(CUSTOMER_ID);
            order.cancel();

            assertThrows(DomainException.class, order::cancel);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // applyPaymentFailure
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("applyPaymentFailure()")
    class ApplyPaymentFailureTests {

        @Test
        @DisplayName("Deve incrementar contador de falhas sem cancelar antes da 3ª falha")
        void shouldIncrementFailureCountWithoutCancelling() {
            Order order = confirmedOrderWithItem();

            order.applyPaymentFailure();
            assertEquals(OrderStatus.CONFIRMADO, order.getStatus());
            assertEquals(1, order.getPaymentFailureCount());

            order.applyPaymentFailure();
            assertEquals(OrderStatus.CONFIRMADO, order.getStatus());
            assertEquals(2, order.getPaymentFailureCount());
        }

        @Test
        @DisplayName("Deve cancelar pedido na 3ª falha de pagamento")
        void shouldCancelOrderOnThirdPaymentFailure() {
            Order order = confirmedOrderWithItem();

            applyPaymentFailuresTimes(order, 3);

            assertEquals(OrderStatus.CANCELADO, order.getStatus());
            assertEquals(3, order.getPaymentFailureCount());
        }

        @Test
        @DisplayName("Deve lançar DomainException ao aplicar falha de pagamento em pedido PENDENTE")
        void shouldThrowWhenApplyingFailureToPendingOrder() {
            Order order = new Order(CUSTOMER_ID);

            assertThrows(DomainException.class, order::applyPaymentFailure);
        }

        @Test
        @DisplayName("Deve lançar DomainException ao aplicar falha de pagamento em pedido CANCELADO")
        void shouldThrowWhenApplyingFailureToCancelledOrder() {
            Order order = confirmedOrderWithItem();
            applyPaymentFailuresTimes(order, 3);

            assertThrows(DomainException.class, order::applyPaymentFailure);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OrderItem
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("OrderItem - Validações")
    class OrderItemTests {

        @Test
        @DisplayName("Deve calcular subtotal corretamente")
        void shouldCalculateSubtotal() {
            OrderItem item = new OrderItem("p1", "Produto", 3, new BigDecimal("15.00"));

            assertEquals(new BigDecimal("45.00"), item.getSubtotal());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException com quantity zero")
        void shouldThrowWhenQuantityIsZero() {
            assertThrows(IllegalArgumentException.class,
                    () -> new OrderItem("p1", "Produto", 0, new BigDecimal("10.00")));
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException com quantity negativa")
        void shouldThrowWhenQuantityIsNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> new OrderItem("p1", "Produto", -1, new BigDecimal("10.00")));
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException com unitPrice negativo")
        void shouldThrowWhenUnitPriceIsNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> new OrderItem("p1", "Produto", 1, new BigDecimal("-1.00")));
        }

        @Test
        @DisplayName("Deve lançar NullPointerException com productId nulo")
        void shouldThrowWhenProductIdIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new OrderItem(null, "Produto", 1, new BigDecimal("10.00")));
        }

        @Test
        @DisplayName("Deve lançar NullPointerException com productName nulo")
        void shouldThrowWhenProductNameIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new OrderItem("p1", null, 1, new BigDecimal("10.00")));
        }

        @Test
        @DisplayName("Deve lançar NullPointerException com unitPrice nulo")
        void shouldThrowWhenUnitPriceIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new OrderItem("p1", "Produto", 1, null));
        }

        @Test
        @DisplayName("equals deve retornar true para o mesmo objeto")
        void equalsShouldReturnTrueForSameObject() {
            OrderItem item = new OrderItem("p1", "Produto", 1, new BigDecimal("10.00"));
            assertEquals(item, item);
        }

        @Test
        @DisplayName("equals deve retornar true para objetos com mesmos valores")
        void equalsShouldReturnTrueForEqualItems() {
            OrderItem item1 = new OrderItem("p1", "Produto", 2, new BigDecimal("10.00"));
            OrderItem item2 = new OrderItem("p1", "Produto", 2, new BigDecimal("10.00"));
            assertEquals(item1, item2);
        }

        @Test
        @DisplayName("equals deve retornar false para objetos com valores diferentes")
        void equalsShouldReturnFalseForDifferentItems() {
            OrderItem item1 = new OrderItem("p1", "Produto", 2, new BigDecimal("10.00"));
            OrderItem item2 = new OrderItem("p2", "Produto", 2, new BigDecimal("10.00"));
            assertNotEquals(item1, item2);
        }

        @Test
        @DisplayName("equals deve retornar false para objeto nulo")
        void equalsShouldReturnFalseForNull() {
            OrderItem item = new OrderItem("p1", "Produto", 1, new BigDecimal("10.00"));
            assertNotEquals(item, null);
        }

        @Test
        @DisplayName("equals deve retornar false para objeto de outro tipo")
        void equalsShouldReturnFalseForDifferentType() {
            OrderItem item = new OrderItem("p1", "Produto", 1, new BigDecimal("10.00"));
            assertNotEquals(item, "string");
        }

        @Test
        @DisplayName("hashCode deve ser igual para objetos iguais")
        void hashCodeShouldBeEqualForEqualItems() {
            OrderItem item1 = new OrderItem("p1", "Produto", 2, new BigDecimal("10.00"));
            OrderItem item2 = new OrderItem("p1", "Produto", 2, new BigDecimal("10.00"));
            assertEquals(item1.hashCode(), item2.hashCode());
        }

        @Test
        @DisplayName("Deve expor todos os atributos via getters")
        void shouldExposeAllAttributesViaGetters() {
            OrderItem item = new OrderItem("p1", "Produto A", 3, new BigDecimal("7.50"));
            assertEquals("p1", item.getProductId());
            assertEquals("Produto A", item.getProductName());
            assertEquals(3, item.getQuantity());
            assertEquals(new BigDecimal("7.50"), item.getUnitPrice());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OrderStatus
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("OrderStatus - Transições")
    class OrderStatusTests {

        @Test
        @DisplayName("PENDENTE pode adicionar itens e confirmar, não pode receber falha de pagamento")
        void pendenteShouldHaveCorrectCapabilities() {
            assertTrue(OrderStatus.PENDENTE.canAddItems());
            assertTrue(OrderStatus.PENDENTE.canConfirm());
            assertFalse(OrderStatus.PENDENTE.canApplyPaymentFailure());
        }

        @Test
        @DisplayName("CONFIRMADO não pode adicionar itens nem confirmar, pode receber falha de pagamento")
        void confirmadoShouldHaveCorrectCapabilities() {
            assertFalse(OrderStatus.CONFIRMADO.canAddItems());
            assertFalse(OrderStatus.CONFIRMADO.canConfirm());
            assertTrue(OrderStatus.CONFIRMADO.canApplyPaymentFailure());
        }

        @Test
        @DisplayName("CANCELADO não pode adicionar itens, confirmar nem receber falha de pagamento")
        void canceladoShouldHaveCorrectCapabilities() {
            assertFalse(OrderStatus.CANCELADO.canAddItems());
            assertFalse(OrderStatus.CANCELADO.canConfirm());
            assertFalse(OrderStatus.CANCELADO.canApplyPaymentFailure());
        }

        @Test
        @DisplayName("PENDENTE pode cancelar")
        void pendenteShouldBeAbleToCancel() {
            assertTrue(OrderStatus.PENDENTE.canCancel());
        }

        @Test
        @DisplayName("CONFIRMADO pode cancelar")
        void confirmadoShouldBeAbleToCancel() {
            assertTrue(OrderStatus.CONFIRMADO.canCancel());
        }

        @Test
        @DisplayName("CANCELADO não pode cancelar novamente")
        void canceladoShouldNotBeAbleToCancel() {
            assertFalse(OrderStatus.CANCELADO.canCancel());
        }

        @Test
        @DisplayName("Deve lançar DomainException ao tentar confirmar CONFIRMADO")
        void shouldThrowWhenTransitioningFromConfirmedToConfirmed() {
            assertThrows(DomainException.class,
                    () -> OrderStatus.CONFIRMADO.transitionTo(OrderStatus.CONFIRMADO));
        }

        @Test
        @DisplayName("Deve lançar DomainException ao tentar confirmar CANCELADO")
        void shouldThrowWhenTransitioningFromCancelledToConfirmed() {
            assertThrows(DomainException.class,
                    () -> OrderStatus.CANCELADO.transitionTo(OrderStatus.CONFIRMADO));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Order confirmedOrderWithItem() {
        Order order = new Order(CUSTOMER_ID);
        order.addItem(buildItem(1, "100.00"));
        order.confirmOrder();
        return order;
    }

    private void applyPaymentFailuresTimes(Order order, int times) {
        for (int i = 0; i < times; i++) {
            order.applyPaymentFailure();
        }
    }
}
