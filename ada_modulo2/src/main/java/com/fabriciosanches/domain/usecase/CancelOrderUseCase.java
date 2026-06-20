package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.model.OrderStatus;
import com.fabriciosanches.domain.port.input.CancelOrderCommand;
import com.fabriciosanches.domain.port.input.CancelOrderUseCasePort;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class CancelOrderUseCase implements CancelOrderUseCasePort {

    private final OrderRepositoryPort orderRepository;

    public CancelOrderUseCase(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void execute(CancelOrderCommand command) {
        if (command == null || command.orderId() == null || command.orderId().isBlank()) {
            throw new DomainException("orderId não pode ser vazio");
        }
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido não encontrado. orderId=" + command.orderId()));
        if (order.getStatus() == OrderStatus.CANCELADO) {
            return;
        }
        order.cancel();
        orderRepository.save(order);
    }
}
