package com.fabriciosanches.adapter.outbound.persistence;

import com.fabriciosanches.adapter.outbound.persistence.entity.PaymentJpaEntity;
import com.fabriciosanches.adapter.outbound.persistence.repository.PaymentJpaRepository;
import com.fabriciosanches.domain.model.Payment;
import com.fabriciosanches.domain.port.output.PaymentRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final PaymentJpaRepository jpaRepository;

    public PaymentRepositoryAdapter(PaymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = toEntity(payment);
        PaymentJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private PaymentJpaEntity toEntity(Payment payment) {
        return new PaymentJpaEntity(
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getCreatedAt());
    }

    private Payment toDomain(PaymentJpaEntity entity) {
        return new Payment(
                entity.getId(),
                entity.getOrderId(),
                entity.getPaymentMethod(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
