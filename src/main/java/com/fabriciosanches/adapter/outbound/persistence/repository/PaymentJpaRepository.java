package com.fabriciosanches.adapter.outbound.persistence.repository;

import com.fabriciosanches.adapter.outbound.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, String> {
}
