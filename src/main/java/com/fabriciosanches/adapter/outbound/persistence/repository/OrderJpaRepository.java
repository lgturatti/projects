package com.fabriciosanches.adapter.outbound.persistence.repository;

import com.fabriciosanches.adapter.outbound.persistence.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, String> {

    List<OrderJpaEntity> findByCustomerId(String customerId);
}
