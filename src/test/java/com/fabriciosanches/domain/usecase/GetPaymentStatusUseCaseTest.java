package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.model.Payment;
import com.fabriciosanches.domain.model.PaymentStatus;
import com.fabriciosanches.domain.port.input.GetPaymentStatusCommand;
import com.fabriciosanches.domain.port.input.GetPaymentStatusResult;
import com.fabriciosanches.domain.port.output.PaymentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GetPaymentStatusUseCase - Testes de Unidade")
class GetPaymentStatusUseCaseTest {

    private PaymentRepositoryPort paymentRepository;
    private GetPaymentStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepositoryPort.class);
        useCase = new GetPaymentStatusUseCase(paymentRepository);
    }

    @Test
    @DisplayName("Deve retornar status do pagamento quando encontrado")
    void shouldReturnPaymentStatusWhenFound() {
        Payment payment = new Payment("pay-001", "order-001", "CREDIT_CARD",
                PaymentStatus.PROCESSING, Instant.now());
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(payment));

        GetPaymentStatusResult result = useCase.execute(new GetPaymentStatusCommand("pay-001"));

        assertNotNull(result);
        assertEquals("pay-001", result.paymentId());
        assertEquals("order-001", result.orderId());
        assertEquals("CREDIT_CARD", result.paymentMethod());
        assertEquals("PROCESSING", result.status());
    }

    @Test
    @DisplayName("Deve retornar pagamento com status APPROVED")
    void shouldReturnApprovedPayment() {
        Payment payment = new Payment("pay-002", "order-002", "PIX",
                PaymentStatus.APPROVED, Instant.now());
        when(paymentRepository.findById("pay-002")).thenReturn(Optional.of(payment));

        GetPaymentStatusResult result = useCase.execute(new GetPaymentStatusCommand("pay-002"));

        assertEquals("APPROVED", result.status());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando pagamento não for encontrado")
    void shouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> useCase.execute(new GetPaymentStatusCommand("unknown")));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando paymentId é nulo")
    void shouldThrowWhenPaymentIdIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new GetPaymentStatusCommand(null)));
        assertTrue(ex.getMessage().contains("paymentId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando paymentId está em branco")
    void shouldThrowWhenPaymentIdIsBlank() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(new GetPaymentStatusCommand("")));
        assertTrue(ex.getMessage().contains("paymentId"));
    }

    @Test
    @DisplayName("Deve lançar DomainException quando comando é nulo")
    void shouldThrowWhenCommandIsNull() {
        assertThrows(DomainException.class, () -> useCase.execute(null));
    }
}
