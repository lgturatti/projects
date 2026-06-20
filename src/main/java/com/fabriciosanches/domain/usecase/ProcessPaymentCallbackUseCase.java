package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.model.Payment;
import com.fabriciosanches.domain.port.input.ProcessPaymentCallbackCommand;
import com.fabriciosanches.domain.port.input.ProcessPaymentCallbackResult;
import com.fabriciosanches.domain.port.input.ProcessPaymentCallbackUseCasePort;
import com.fabriciosanches.domain.port.output.NotificationClientPort;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import com.fabriciosanches.domain.port.output.PaymentRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class ProcessPaymentCallbackUseCase implements ProcessPaymentCallbackUseCasePort {

    private final PaymentRepositoryPort paymentRepository;
    private final OrderRepositoryPort orderRepository;
    private final NotificationClientPort notificationClient;

    public ProcessPaymentCallbackUseCase(PaymentRepositoryPort paymentRepository,
                                         OrderRepositoryPort orderRepository,
                                         NotificationClientPort notificationClient) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.notificationClient = notificationClient;
    }

    @Override
    public ProcessPaymentCallbackResult execute(ProcessPaymentCallbackCommand command) {
        if (command == null || command.paymentId() == null || command.paymentId().isBlank()) {
            throw new DomainException("paymentId não pode ser vazio");
        }
        if (command.callbackStatus() == null || command.callbackStatus().isBlank()) {
            throw new DomainException("callbackStatus não pode ser vazio");
        }
        Payment payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pagamento não encontrado. paymentId=" + command.paymentId()));
        if (payment.isAlreadyProcessed()) {
            return new ProcessPaymentCallbackResult(payment.getId(), payment.getStatus().name());
        }
        String normalizedStatus = command.callbackStatus().toUpperCase();
        if ("APPROVED".equals(normalizedStatus)) {
            Order order = orderRepository.findById(payment.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Pedido não encontrado. orderId=" + payment.getOrderId()));
            payment.approve();
            Payment saved = paymentRepository.save(payment);
            notificationClient.notifyOrderApproved(order.getId(), order.getCustomerId());
            return new ProcessPaymentCallbackResult(saved.getId(), saved.getStatus().name());
        } else if ("REJECTED".equals(normalizedStatus)) {
            payment.reject();
            Order order = orderRepository.findById(payment.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Pedido não encontrado. orderId=" + payment.getOrderId()));
            order.applyPaymentFailure();
            orderRepository.save(order);
        } else {
            throw new DomainException("callbackStatus inválido: " + command.callbackStatus());
        }
        Payment saved = paymentRepository.save(payment);
        return new ProcessPaymentCallbackResult(saved.getId(), saved.getStatus().name());
    }
}
