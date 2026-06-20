package com.fabriciosanches.domain.model;

import com.fabriciosanches.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment - Testes de domínio")
class PaymentTest {

    @Test
    @DisplayName("Deve criar pagamento em status PROCESSING")
    void shouldCreatePaymentWithProcessingStatus() {
        Payment payment = new Payment("order-001", "pix");

        assertNotNull(payment.getId());
        assertEquals("order-001", payment.getOrderId());
        assertEquals("pix", payment.getPaymentMethod());
        assertEquals(PaymentStatus.PROCESSING, payment.getStatus());
        assertNotNull(payment.getCreatedAt());
        assertFalse(payment.isAlreadyProcessed());
    }

    @Test
    @DisplayName("Deve reconstruir pagamento via construtor completo")
    void shouldReconstructPaymentViaFullConstructor() {
        Instant now = Instant.now();
        Payment payment = new Payment("id-001", "order-001", "pix", PaymentStatus.APPROVED, now);

        assertEquals("id-001", payment.getId());
        assertEquals("order-001", payment.getOrderId());
        assertEquals("pix", payment.getPaymentMethod());
        assertEquals(PaymentStatus.APPROVED, payment.getStatus());
        assertEquals(now, payment.getCreatedAt());
        assertTrue(payment.isAlreadyProcessed());
    }

    @Test
    @DisplayName("Deve lançar NullPointerException ao criar pagamento com orderId nulo")
    void shouldThrowWhenOrderIdIsNull() {
        assertThrows(NullPointerException.class, () -> new Payment(null, "pix"));
    }

    @Test
    @DisplayName("Deve lançar NullPointerException ao criar pagamento com paymentMethod nulo")
    void shouldThrowWhenPaymentMethodIsNull() {
        assertThrows(NullPointerException.class, () -> new Payment("order-001", null));
    }

    @Nested
    @DisplayName("approve()")
    class ApproveTests {

        @Test
        @DisplayName("Deve aprovar pagamento em PROCESSING")
        void shouldApproveProcessingPayment() {
            Payment payment = new Payment("order-001", "pix");

            payment.approve();

            assertEquals(PaymentStatus.APPROVED, payment.getStatus());
            assertTrue(payment.isAlreadyProcessed());
        }

        @Test
        @DisplayName("Deve lançar DomainException ao aprovar pagamento já processado")
        void shouldThrowWhenApprovingAlreadyProcessedPayment() {
            Payment payment = new Payment("order-001", "pix");
            payment.approve();

            assertThrows(DomainException.class, payment::approve);
        }
    }

    @Nested
    @DisplayName("reject()")
    class RejectTests {

        @Test
        @DisplayName("Deve rejeitar pagamento em PROCESSING")
        void shouldRejectProcessingPayment() {
            Payment payment = new Payment("order-001", "pix");

            payment.reject();

            assertEquals(PaymentStatus.REJECTED, payment.getStatus());
            assertTrue(payment.isAlreadyProcessed());
        }

        @Test
        @DisplayName("Deve lançar DomainException ao rejeitar pagamento já processado")
        void shouldThrowWhenRejectingAlreadyProcessedPayment() {
            Payment payment = new Payment("order-001", "pix");
            payment.reject();

            assertThrows(DomainException.class, payment::reject);
        }
    }
}
