package com.fabriciosanches.adapter.outbound.persistence.repository;

import com.fabriciosanches.adapter.outbound.persistence.entity.IdempotencyKeyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyJpaEntity, String> {
}
