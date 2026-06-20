package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.port.input.GetOrderByIdResult;
import com.fabriciosanches.domain.port.input.ListOrdersByCustomerCommand;
import com.fabriciosanches.domain.port.input.ListOrdersByCustomerResult;
import com.fabriciosanches.domain.port.input.ListOrdersByCustomerUseCasePort;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListOrdersByCustomerUseCase implements ListOrdersByCustomerUseCasePort {

    private final OrderRepositoryPort orderRepository;

    public ListOrdersByCustomerUseCase(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public ListOrdersByCustomerResult execute(ListOrdersByCustomerCommand command) {
        if (command == null || command.customerId() == null || command.customerId().isBlank()) {
            throw new DomainException("customerId não pode ser vazio");
        }
        List<Order> orders = orderRepository.findByCustomerId(command.customerId());
        List<GetOrderByIdResult> results = orders.stream()
                .map(OrderMapper::toResult)
                .toList();
        return new ListOrdersByCustomerResult(results);
    }
}
