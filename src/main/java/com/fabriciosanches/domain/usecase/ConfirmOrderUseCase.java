package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.model.OrderStatus;
import com.fabriciosanches.domain.port.input.ConfirmOrderCommand;
import com.fabriciosanches.domain.port.input.ConfirmOrderUseCasePort;
import com.fabriciosanches.domain.port.input.GetOrderByIdResult;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class ConfirmOrderUseCase implements ConfirmOrderUseCasePort {

    private final OrderRepositoryPort orderRepository;

    public ConfirmOrderUseCase(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public GetOrderByIdResult execute(ConfirmOrderCommand command) {
        if (command == null || command.orderId() == null || command.orderId().isBlank()) {
            throw new DomainException("orderId não pode ser vazio");
        }
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido não encontrado. orderId=" + command.orderId()));
        if (order.getStatus() == OrderStatus.CONFIRMADO) {
            return OrderMapper.toResult(order);
        }
        order.confirmOrder();
        Order saved = orderRepository.save(order);
        return OrderMapper.toResult(saved);
    }
}
