package com.fabriciosanches.domain.usecase;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.model.OrderItem;
import com.fabriciosanches.domain.port.input.AddItemToOrderCommand;
import com.fabriciosanches.domain.port.input.AddItemToOrderUseCasePort;
import com.fabriciosanches.domain.port.input.GetOrderByIdResult;
import com.fabriciosanches.domain.port.output.CatalogClientPort;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AddItemToOrderUseCase implements AddItemToOrderUseCasePort {

    private final OrderRepositoryPort orderRepository;
    private final CatalogClientPort catalogClient;

    public AddItemToOrderUseCase(OrderRepositoryPort orderRepository, CatalogClientPort catalogClient) {
        this.orderRepository = orderRepository;
        this.catalogClient = catalogClient;
    }

    @Override
    public GetOrderByIdResult execute(AddItemToOrderCommand command) {
        if (command == null || command.orderId() == null || command.orderId().isBlank()) {
            throw new DomainException("orderId não pode ser vazio");
        }
        if (command.productId() == null || command.productId().isBlank()) {
            throw new DomainException("productId não pode ser vazio");
        }
        if (command.productName() == null || command.productName().isBlank()) {
            throw new DomainException("productName não pode ser vazio");
        }
        if (command.quantity() <= 0) {
            throw new DomainException("quantity deve ser maior que zero");
        }
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido não encontrado. orderId=" + command.orderId()));
        catalogClient.validateStock(command.productId(), command.quantity());
        BigDecimal unitPrice = catalogClient.getProductPrice(command.productId());
        OrderItem item = new OrderItem(command.productId(), command.productName(), command.quantity(), unitPrice);
        order.addItem(item);
        Order saved = orderRepository.save(order);
        return OrderMapper.toResult(saved);
    }
}
