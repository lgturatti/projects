package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.port.input.GetOrderByIdCommand;
import com.fabriciosanches.domain.port.input.GetOrderByIdResult;
import com.fabriciosanches.domain.port.input.GetOrderByIdUseCasePort;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class GetOrderByIdUseCase implements GetOrderByIdUseCasePort {

    private final OrderRepositoryPort orderRepository;

    public GetOrderByIdUseCase(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public GetOrderByIdResult execute(GetOrderByIdCommand command) {
        if (command == null || command.orderId() == null || command.orderId().isBlank()) {
            throw new DomainException("orderId não pode ser vazio");
        }
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido não encontrado. orderId=" + command.orderId()));
        return OrderMapper.toResult(order);
    }
}
