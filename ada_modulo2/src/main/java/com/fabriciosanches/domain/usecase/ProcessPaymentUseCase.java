package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.model.Payment;
import com.fabriciosanches.domain.port.input.ProcessPaymentCommand;
import com.fabriciosanches.domain.port.input.ProcessPaymentResult;
import com.fabriciosanches.domain.port.input.ProcessPaymentUseCasePort;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import com.fabriciosanches.domain.port.output.PaymentGatewayClientPort;
import com.fabriciosanches.domain.port.output.PaymentRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class ProcessPaymentUseCase implements ProcessPaymentUseCasePort {

    private final OrderRepositoryPort orderRepository;
    private final PaymentRepositoryPort paymentRepository;
    private final PaymentGatewayClientPort paymentGatewayClient;

    public ProcessPaymentUseCase(OrderRepositoryPort orderRepository,
                                 PaymentRepositoryPort paymentRepository,
                                 PaymentGatewayClientPort paymentGatewayClient) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGatewayClient = paymentGatewayClient;
    }

    @Override
    public ProcessPaymentResult execute(ProcessPaymentCommand command) {
        if (command == null || command.orderId() == null || command.orderId().isBlank()) {
            throw new DomainException("orderId não pode ser vazio");
        }
        if (command.paymentMethod() == null || command.paymentMethod().isBlank()) {
            throw new DomainException("paymentMethod não pode ser vazio");
        }
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new DomainException("Pedido não encontrado. orderId=" + command.orderId()));
        if (!order.getStatus().canApplyPaymentFailure()) {
            throw new DomainException(
                    String.format("Não é possível processar pagamento de um pedido com status '%s'.", order.getStatus()));
        }
        paymentGatewayClient.charge(command.orderId(), order.getTotalAmount(), command.paymentMethod());
        Payment payment = new Payment(command.orderId(), command.paymentMethod());
        Payment saved = paymentRepository.save(payment);
        return new ProcessPaymentResult(saved.getId(), saved.getStatus().name());
    }
}
