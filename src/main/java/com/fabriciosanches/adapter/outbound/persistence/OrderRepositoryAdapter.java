package com.fabriciosanches.adapter.outbound.persistence;

import com.fabriciosanches.adapter.outbound.persistence.entity.OrderItemJpaEntity;
import com.fabriciosanches.adapter.outbound.persistence.entity.OrderJpaEntity;
import com.fabriciosanches.adapter.outbound.persistence.repository.OrderJpaRepository;
import com.fabriciosanches.domain.model.Order;
import com.fabriciosanches.domain.model.OrderItem;
import com.fabriciosanches.domain.port.output.OrderRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository jpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderJpaEntity entity = toEntity(order);
        OrderJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByCustomerId(String customerId) {
        return jpaRepository.findByCustomerId(customerId).stream()
                .map(this::toDomain)
                .toList();
    }

    private OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getPaymentFailureCount(),
                order.getCreatedAt()
        );
        order.getItems().stream()
                .map(item -> new OrderItemJpaEntity(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice()))
                .forEach(entity.getItems()::add);
        return entity;
    }

    private Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(item -> new OrderItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice()))
                .toList();
        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getCreatedAt(),
                items,
                entity.getStatus(),
                entity.getPaymentFailureCount(),
                entity.getTotalAmount()
        );
    }
}
