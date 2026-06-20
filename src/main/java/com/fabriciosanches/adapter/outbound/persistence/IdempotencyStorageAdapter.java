package com.fabriciosanches.adapter.outbound.persistence;

import com.fabriciosanches.adapter.outbound.persistence.entity.IdempotencyKeyJpaEntity;
import com.fabriciosanches.adapter.outbound.persistence.repository.IdempotencyKeyJpaRepository;
import com.fabriciosanches.domain.port.output.IdempotencyStoragePort;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class IdempotencyStorageAdapter implements IdempotencyStoragePort {

    private final IdempotencyKeyJpaRepository jpaRepository;

    public IdempotencyStorageAdapter(IdempotencyKeyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean exists(String idempotencyKey) {
        return jpaRepository.existsById(idempotencyKey);
    }

    @Override
    public void store(String idempotencyKey) {
        jpaRepository.save(new IdempotencyKeyJpaEntity(idempotencyKey, Instant.now()));
    }
}
