package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.port.input.CreateOrderCommand;
import com.fabriciosanches.domain.port.input.CreateOrderResult;
import com.fabriciosanches.domain.port.input.CreateOrderUseCasePort;
import com.fabriciosanches.domain.port.output.CustomerClientPort;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class CreateOrderUseCase implements CreateOrderUseCasePort {

    private final OrderRepositoryPort orderRepository;
    private final CustomerClientPort customerClient;

    public CreateOrderUseCase(OrderRepositoryPort orderRepository, CustomerClientPort customerClient) {
        this.orderRepository = orderRepository;
        this.customerClient = customerClient;
    }

    @Override
    public CreateOrderResult execute(CreateOrderCommand command) {
        if (command == null || command.customerId() == null || command.customerId().isBlank()) {
            throw new DomainException("customerId não pode ser vazio");
        }
        customerClient.validateCustomer(command.customerId());
        Order order = new Order(command.customerId());
        Order saved = orderRepository.save(order);
        return new CreateOrderResult(saved.getId());
    }
}
