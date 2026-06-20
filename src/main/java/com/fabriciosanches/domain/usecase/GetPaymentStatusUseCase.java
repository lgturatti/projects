package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.model.Payment;
import com.fabriciosanches.domain.port.input.GetPaymentStatusCommand;
import com.fabriciosanches.domain.port.input.GetPaymentStatusResult;
import com.fabriciosanches.domain.port.input.GetPaymentStatusUseCasePort;
import com.fabriciosanches.domain.port.output.PaymentRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class GetPaymentStatusUseCase implements GetPaymentStatusUseCasePort {

    private final PaymentRepositoryPort paymentRepository;

    public GetPaymentStatusUseCase(PaymentRepositoryPort paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public GetPaymentStatusResult execute(GetPaymentStatusCommand command) {
        if (command == null || command.paymentId() == null || command.paymentId().isBlank()) {
            throw new DomainException("paymentId não pode ser vazio");
        }
        Payment payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pagamento não encontrado. paymentId=" + command.paymentId()));
        return new GetPaymentStatusResult(
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentMethod(),
                payment.getStatus().name(),
                payment.getCreatedAt());
    }
}
