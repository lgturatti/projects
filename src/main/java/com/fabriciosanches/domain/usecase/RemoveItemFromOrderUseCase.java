package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.port.input.RemoveItemFromOrderCommand;
import com.fabriciosanches.domain.port.input.RemoveItemFromOrderUseCasePort;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class RemoveItemFromOrderUseCase implements RemoveItemFromOrderUseCasePort {

    private final OrderRepositoryPort orderRepository;

    public RemoveItemFromOrderUseCase(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void execute(RemoveItemFromOrderCommand command) {
        if (command == null || command.orderId() == null || command.orderId().isBlank()) {
            throw new DomainException("orderId não pode ser vazio");
        }
        if (command.productId() == null || command.productId().isBlank()) {
            throw new DomainException("productId não pode ser vazio");
        }
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido não encontrado. orderId=" + command.orderId()));
        order.removeItem(command.productId());
        orderRepository.save(order);
    }
}
